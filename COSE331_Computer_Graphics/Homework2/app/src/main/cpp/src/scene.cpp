#include "scene.h"
#include "binary/animation.h"
#include "binary/skeleton.h"
#include "binary/player.h"

#include <math.h>

Shader* Scene::vertexShader = nullptr;
Shader* Scene::fragmentShader = nullptr;
Program* Scene::program = nullptr;
Camera* Scene::camera = nullptr;
Object* Scene::player = nullptr;
Texture* Scene::diffuse = nullptr;
Texture* Scene::normal = nullptr;
Material* Scene::material = nullptr;
Object* Scene::lineDraw = nullptr;
Texture* Scene::lineColor = nullptr;
Material* Scene::lineMaterial = nullptr;

vector<vec3>* playertangents = nullptr;

bool Scene::buttonFlag = true;

void Scene::setup(AAssetManager* aAssetManager) {
    Asset::setManager(aAssetManager);

    Scene::vertexShader = new Shader(GL_VERTEX_SHADER, "vertex.glsl");
    Scene::fragmentShader = new Shader(GL_FRAGMENT_SHADER, "fragment.glsl");

    Scene::program = new Program(Scene::vertexShader, Scene::fragmentShader);

    Scene::camera = new Camera(Scene::program);
    Scene::camera->eye = vec3(0.0f, 0.0f, 80.0f);

    Scene::diffuse = new Texture(Scene::program, 0, "colorMap", playerTexels, playerSize);
    Scene::material = new Material(Scene::program, diffuse);

    playertangents = new vector<vec3>(playerVertices.size(), vec3(0.0f));
    Scene::player = new Object(program, material, playerVertices, playerIndices, *playertangents);

    player->calculateTangents(*playertangents);
    player->load(playerVertices, playerIndices, *playertangents);
    player->worldMat = scale(vec3(1.0f / 3.0f));

    Scene::normal = new Texture(Scene::program, 1, "normalMap", playerNormal, playerSize);
    normal->update();


//    Scene::lineColor = new Texture(Scene::program, 0, "ColorMap", {{0xFF, 0x00, 0x00}}, 1);
//    Scene::lineMaterial = new Material(Scene::program, lineColor);
//    Scene::lineDraw = new Object(program, lineMaterial, {{}}, {{}}, GL_LINES);

    // provide lightDir variable
    GLint lightDirtLoc = glGetUniformLocation(program->get(), "lightDir");
    if (lightDirtLoc >= 0) glUniform3f(lightDirtLoc, camera->eye.x,camera->eye.y, camera->eye.z);
}

void Scene::screen(int width, int height) {
    Scene::camera->aspect = (float) width/height;
}

void Scene::update(float deltaTime) {
    Scene::program->use();
    Scene::camera->update();

    // --------  My Code --------- //

    static float elapsed_time = 0;
    elapsed_time += deltaTime;

    if (elapsed_time > 4.0) elapsed_time -= 4.0;

    int prev_key_num = (int)floor(elapsed_time);
    int next_key_num = (prev_key_num + 1) % 4;

    vector<float> prev_frame = motions[prev_key_num];
    vector<float> next_frame = motions[next_key_num];

    float t = elapsed_time - prev_key_num;  // t is in [0, 1]

    vec3 prev_trans = vec3(prev_frame[0], prev_frame[1], prev_frame[2]);
    vec3 next_trans = vec3(next_frame[0], next_frame[1], next_frame[2]);

    mat4 root_trans = glm::translate(glm::mix(prev_trans, next_trans, t));

//    vector<Vertex> animated_player_joints;
    vector<mat4> Ml;
    vector<mat4> Md;
    vector<mat4> Ma;
    vector<mat4> M;

    // Calculate Ml for each bone
    for (int i = 0; i < 27; i++)
    {
        mat4 prev_rot_X = glm::rotate(radians(prev_frame[i * 3 + 3]), vec3(1, 0, 0));
        mat4 prev_rot_Y = glm::rotate(radians(prev_frame[i * 3 + 4]), vec3(0, 1, 0));
        mat4 prev_rot_Z = glm::rotate(radians(prev_frame[i * 3 + 5]), vec3(0, 0, 1));
        mat4 prev_rot = prev_rot_Z * prev_rot_X * prev_rot_Y;

        mat4 next_rot_X = glm::rotate(radians(next_frame[i * 3 + 3]), vec3(1, 0, 0));
        mat4 next_rot_Y = glm::rotate(radians(next_frame[i * 3 + 4]), vec3(0, 1, 0));
        mat4 next_rot_Z = glm::rotate(radians(next_frame[i * 3 + 5]), vec3(0, 0, 1));
        mat4 next_rot = next_rot_Z * next_rot_X * next_rot_Y;

        quat prev_quat = glm::quat_cast(prev_rot);
        quat next_quat = glm::quat_cast(next_rot);

        quat interpolated_quat = glm::slerp(prev_quat, next_quat, t);

        Ml.push_back(glm::mat4_cast(interpolated_quat));

    }

    Ml[0] = root_trans * Ml[0];

    static mat4 prev_neck_motion = Ml[13];
    static mat4 prev_head_motion = Ml[14];
    static mat4 prev_hnub_motion = Ml[15];

    if (!Scene::buttonFlag)
    {
        Ml[13] = prev_neck_motion;
        Ml[14] = prev_head_motion;
        Ml[15] = prev_hnub_motion;
    }

    prev_neck_motion = Ml[13];
    prev_head_motion = Ml[14];
    prev_hnub_motion = Ml[15];


    // Calculate Animated Joints
    Md.emplace_back();  // Md[0] = Identity Mat
    Ma.emplace_back();  // Ma[0] = Identity Mat
    M.emplace_back();   // M[0] = Identity Mat
    for (int i = 1; i < 27; i++)
    {
        // Calculate Md
        mat4 Mp = glm::translate(jOffsets[i]);
        mat4 Mdi = Md[jParents[i]] * Mp;
        Md.push_back(Mdi);

        // Calculate Ma
        mat4 Mai = Ma[jParents[i]] * Mp * Ml[i];
        Ma.push_back(Mai);

        // Calculate M
        M.push_back(Mai * inverse(Mdi));
    }

//    // for skeleton line draw
//    vector<Index> lineDraw_index;
//    for (int i = 0; i < 27; i++)
//    {
//        vec3 animated_joint_pos = vec3(Ma[i] * vec4(0.0, 0.0, 0.0, 1.0));
//        Vertex v;
//        v.pos = animated_joint_pos;
//        animated_player_joints.push_back(v);
//        lineDraw_index.push_back(i);
//        lineDraw_index.push_back(jParents[i]);
//    }

    // Skinning
    vector<Vertex> animated_player_vertices;
    for (Vertex v : playerVertices)
    {
        vec3 skinned_pos = vec3(0.0);
        vec3 skinned_nor = vec3(0.0);
        for (int i = 0; i < 4; i++)
        {
            int bone_index = v.bone[i];
            if (bone_index == -1) continue;
            skinned_pos += vec3((v.weight[i] * M[bone_index] * vec4(v.pos, 1.0)));
            skinned_nor += vec3(v.weight[i] * transpose(inverse(mat3(M[bone_index]))) * v.nor);
        }
        Vertex skinned_vertex = v;
        skinned_vertex.pos = skinned_pos;
        skinned_vertex.nor = skinned_nor;
        animated_player_vertices.push_back(skinned_vertex);
    }

    Scene::player->vertices = animated_player_vertices;

    // --------------------------- //

    // Line Drawer
//    glLineWidth(20);
//    Scene::lineDraw->load(animated_player_joints, lineDraw_index);
//    Scene::lineDraw->draw();

    player->calculateTangents(*playertangents);
    Scene::player->load(animated_player_vertices, playerIndices, *playertangents);
    Scene::player->draw();

}

void Scene::setButtonFlag(bool flag)
{
    Scene::buttonFlag = flag;
}




