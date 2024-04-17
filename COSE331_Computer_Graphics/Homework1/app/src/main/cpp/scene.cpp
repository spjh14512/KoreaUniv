#include "scene.h"

#include "obj_teapot.h"
#include "tex_flower.h"
#include "Requirement.h"

Shader* Scene::vertexShader = nullptr;
Shader* Scene::fragmentShader = nullptr;
Program* Scene::program = nullptr;
Camera* Scene::camera = nullptr;
Light* Scene::light = nullptr;
Object* Scene::teapot = nullptr;
Material* Scene::flower = nullptr;
Object* Scene::lineDraw = nullptr;

void Scene::setup(AAssetManager* aAssetManager) {

    // set asset manager
    Asset::setManager(aAssetManager);


    // create shaders
    vertexShader = new Shader(GL_VERTEX_SHADER, "vertex.glsl");
    fragmentShader = new Shader(GL_FRAGMENT_SHADER, "fragment.glsl");

    // create program
    program = new Program(vertexShader, fragmentShader);

    // create camera
    camera = new Camera(program);
    camera->eye = vec3(60.0f, 00.0f, 0.0f);
    camera->updateCameraUVN();

    // create light
    light = new Light(program);
    light->position = vec3(100.0f, 0.0f, 0.0f);

    // create floral texture
    flower = new Material(program, texFlowerData, texFlowerSize);

    // create teapot object
    teapot = new Object(program, flower, objTeapotVertices, objTeapotIndices,
                        objTeapotVerticesSize, objTeapotIndicesSize, GL_TRIANGLES);


    // Create arbitrary axis
    lineDraw = new Object(program, flower, lineVertices, lineIndices,lineVerticesSize, lineIndicesSize, GL_TRIANGLES);

    //////////////////////////////
    /* TODO: Problem 2.
     *  Scale the teapot by a given scaling factor along the arbitrary axis defined by two
     *  points e.g., (10, 0, 0) and (20, 10, 0). Both the scaling factor and the axis are
     *  provided in a header file (app/src/main/cpp/Requirement.h).
     */

    mat4 scaleM;
    // In OpenGL, the matrix must be transposed

    scaleM = transpose(mat4(    // scale along x-axis
            scaleArbitrary, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
            ));
    mat4 T = transpose(mat4(
            1, 0, 0, startingPoints[0],
            0, 1, 0, startingPoints[1],
            0, 0, 1, startingPoints[2],
            0, 0, 0, 1
            ));
    vec3 u = normalize(vec3(endingPoints[0] - startingPoints[0], endingPoints[1] - startingPoints[1], endingPoints[2] - startingPoints[2]));
    // check if u is parallel to y-axis
    vec3 v = (u.x == 0 and u.z == 0) ? normalize(cross(u, vec3(1, 0, 0))) : normalize(cross(u, vec3(0, 1, 0)));
    vec3 n = cross(u, v);
    mat4 R = mat4(vec4(u, 0), vec4(v, 0), vec4(n, 0), vec4(0, 0, 0, 1));

    teapot->worldMatrix = T * R * scaleM * transpose(R) * inverse(T) * teapot->worldMatrix;
    //////////////////////////////
}

void Scene::screen(int width, int height) {

    // set camera aspect ratio
    camera->aspect = (float) width / height;
}

void Scene::update(float deltaTime) {


    // use program
    program->use();

    //////////////////////////////
    /* TODO: Problem 3.
     *  Keep rotating the teapot clockwise about the rotation axis defined by two points, e.g.,
     *  (10, 0, 0) and (20, 10, 0). The axis is provided in a header
     *  file (app/src/main/cpp/Requirement.h).
     */

    mat4 rotMat;

    rotMat = transpose(mat4(    // rotate along x-axis
            1, 0, 0, 0,
            0, cos(deltaTime), -sin(-deltaTime), 0,
            0, sin(-deltaTime), cos(deltaTime), 0,
            0, 0, 0, 1
            ));
    mat4 T = transpose(mat4(
            1, 0, 0, startingPoints[0],
            0, 1, 0, startingPoints[1],
            0, 0, 1, startingPoints[2],
            0, 0, 0, 1
            ));
    vec3 u = normalize(vec3(endingPoints[0] - startingPoints[0], endingPoints[1] - startingPoints[1], endingPoints[2] - startingPoints[2]));
    // check if u is parallel to y-axis
    vec3 v = (u.x == 0 and u.z == 0) ? normalize(cross(u, vec3(1, 0, 0))) : normalize(cross(u, vec3(0, 1, 0)));
    vec3 n = cross(u, v);
    mat4 R = mat4(vec4(u, 0), vec4(v, 0), vec4(n, 0), vec4(0, 0, 0, 1));

    teapot->worldMatrix = T * R * rotMat * transpose(R) * inverse(T) * teapot->worldMatrix;
    //////////////////////////////

    camera->updateViewMatrix();
    camera->updateProjectionMatrix();
    light->setup();


    // draw teapot
    teapot->draw();
    lineDraw->draw();

}

void Scene::rotateCamera(float dx,float dy) {
    float rotationSensitivity = 0.03;

    float thetaYaw=glm::radians(rotationSensitivity*dx);
    float thetaPinch=glm::radians(rotationSensitivity*dy);

    rotateCameraYaw(thetaYaw);
    rotateCameraPitch(thetaPinch);

}

void Scene::rotateCameraYaw(float theta) {


    /*  calculate the rotated u,n vector about v axis.
     *  Argument theta is amount of rotation. theta is positive when CCW.
     *  Note that u,v,n should always be orthonormal.
     *
     *  The u vector can be accessed via camera->cameraU.
     *  The v vector can be accessed via camera->cameraV.
     *  The n vector can be accessed via camera->cameraN.
     */

    glm::mat4 transform = glm::rotate(glm::mat4(1),theta,camera->cameraV);
    camera->cameraU=glm::vec3(transform*vec4(camera->cameraU,1));
    camera->cameraU.y=0;
    camera->cameraV=glm::normalize(camera->cameraV);
    camera->cameraN=glm::cross(camera->cameraU , camera->cameraV );

    camera->updateViewMatrix();
}

void Scene::rotateCameraPitch(float theta) {

    /*  calculate the rotated v,n vector about u axis in CCW.
      *  Argument theta is amount of rotation. Theta is positive when CCW.
      *  Note that u,v,n should always be orthonormal.
      *
      *  The u vector can be accessed via camera->cameraU.
      *  The v vector can be accessed via camera->cameraV.
      *  The n vector can be accessed via camera->cameraN.
      */

    glm::mat4 transform = glm::rotate(glm::mat4(1),theta,camera->cameraU);
    camera->cameraN=glm::vec3(transform*vec4(camera->cameraN,1));
    camera->cameraV=glm::cross(camera->cameraN,camera->cameraU);

    camera->updateViewMatrix();
}

void Scene::translateLeft(float amount) {

    /*  Calculate the camera position(eye) when translated left.
     *
     */
    camera->eye = camera->eye - amount * camera->cameraU;
    camera->updateViewMatrix();


}

void Scene::translateFront(float amount) {

      /*  Calculate the camera position(eye) when translated Front.
       *
       */
    camera->eye = camera->eye -amount* camera->cameraN;
    camera->updateViewMatrix();

}

void Scene::translateRight(float amount) {

    /*  Calculate the camera position(eye) when translated Right.
    *
    */
    camera->eye = camera->eye + amount * camera->cameraU;
    camera->updateViewMatrix();

}

void Scene::translateBack(float amount) {

    /*  Calculate the camera position(eye) when translated back.
    *
    */
    camera->eye = camera->eye + amount* camera->cameraN;
    camera->updateViewMatrix();

}
