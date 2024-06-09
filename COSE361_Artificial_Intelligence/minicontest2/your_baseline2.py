# myTeam.py
# ---------
# Licensing Information:  You are free to use or extend these projects for
# educational purposes provided that (1) you do not distribute or publish
# solutions, (2) you retain this notice, and (3) you provide clear
# attribution to UC Berkeley, including a link to http://ai.berkeley.edu.
# 
# Attribution Information: The Pacman AI projects were developed at UC Berkeley.
# The core projects and autograders were primarily created by John DeNero
# (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# Student side autograding was added by Brad Miller, Nick Hay, and
# Pieter Abbeel (pabbeel@cs.berkeley.edu).


from captureAgents import CaptureAgent
import random, time, util
from game import Directions
import game


#################
# Team creation #
#################

def createTeam(firstIndex, secondIndex, isRed,
               first='DummyAgent', second='DummyAgent'):
    """
  This function should return a list of two agents that will form the
  team, initialized using firstIndex and secondIndex as their agent
  index numbers.  isRed is True if the red team is being created, and
  will be False if the blue team is being created.

  As a potentially helpful development aid, this function can take
  additional string-valued keyword arguments ("first" and "second" are
  such arguments in the case of this function), which will come from
  the --redOpts and --blueOpts command-line arguments to capture.py.
  For the nightly contest, however, your team will be created without
  any extra arguments, so you should make sure that the default
  behavior is what you want for the nightly contest.
  """

    # The following line is an example only; feel free to change it.
    return [eval(first)(firstIndex), eval(second)(secondIndex)]


##########
# Agents #
##########

class DummyAgent(CaptureAgent):
    """
    A Dummy agent to serve as an example of the necessary agent structure.
    You should look at baselineTeam.py for more details about how to
    create an agent as this is the bare minimum.
    """

    def registerInitialState(self, gameState):
        CaptureAgent.registerInitialState(self, gameState)

        self.myTeam = self.getTeam(gameState)
        self.opponents = self.getOpponents(gameState)
        self.walls = gameState.getWalls()

    def chooseAction(self, gameState):

        def chooseActionOffensive(state, enemyPositions):

            currentPos = state.getAgentPosition(self.index)
            distToEnemy = min(self.getMazeDistance(currentPos, ep) for ep in enemyPositions)
            if distToEnemy < 3:
                # 이 경우 가장 가까운 상대 Agent와 1:1 minimax 수행하여 도망치기

            if self.index % 2 == 0:  # red team
                opFoods = state.getBlueFood()
            else:
                opFoods = state.getRedFood()

            actions = state.getLegalActions(self.index)
            nearestFoodDist = 999999
            nearestFoodAction = "Stop"

            for action in actions:
                successor = state.generateSuccessor(self.index, action)
                successorState = successor.getAgentState(self.index)
                successorPos = tuple(map(int, successorState.getPosition()))

                distToFood = nearest_food(int(successorPos[0]), int(successorPos[1]), opFoods)

                if distToFood < nearestFoodDist:
                    nearestFoodDist = distToFood
                    nearestFoodAction = action

            return nearestFoodAction

        def nearest_food(x, y, food):
            q = util.Queue()
            dist = 1
            q.push((x, y))
            visited = [[False for _y in range(food.height)] for _x in range(food.width)]
            visited[x][y] = True
            dx = [1, -1, 0, 0]
            dy = [0, 0, 1, -1]
            while not q.isEmpty():
                for qSize in range(len(q.list)):
                    cur = q.pop()
                    if food[cur[0]][cur[1]]:
                        return dist
                    for d in range(4):
                        nx = cur[0] + dx[d]
                        ny = cur[1] + dy[d]
                        if 0 <= nx < food.width and 0 <= ny < food.height and not self.walls[nx][ny]:
                            if not visited[nx][ny]:
                                visited[nx][ny] = True
                                q.push((nx, ny))
                dist += 1
            return -1

        def chooseActionDefensive(state, invaders, agent_index):

            actions = state.getLegalActions(agent_index)

            nearestInvaderDist = 999999
            nearestInvaderAction = "Stop"

            for action in actions:
                successor = state.generateSuccessor(agent_index, action)
                successorState = successor.getAgentState(agent_index)
                successorPos = successorState.getPosition()

                dists = [self.getMazeDistance(successorPos, a.getPosition()) for a in invaders]
                dist = min(dists)
                if dist < nearestInvaderDist:
                    nearestInvaderDist = dist
                    nearestInvaderAction = action

            return nearestInvaderDist, nearestInvaderAction

        ###############################################################

        enemies = [gameState.getAgentState(i) for i in self.opponents]
        enemyPositions = [e.getPosition() for e in enemies]

        invaders = [a for a in enemies if a.isPacman and a.getPosition() is not None]

        ret = chooseActionOffensive(gameState, enemyPositions)

        if len(invaders) > 0:
            myDefense = chooseActionDefensive(gameState, invaders, self.index)
            anotherDefense = chooseActionDefensive(gameState, invaders, (self.index + 2) % 4)

            if myDefense[0] < anotherDefense[0]:
                ret = myDefense[1]

        return ret
