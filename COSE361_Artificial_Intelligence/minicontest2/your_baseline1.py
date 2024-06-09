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
               first='MinimaxAgent', second='MinimaxAgent'):
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

class MinimaxAgent(CaptureAgent):

    def registerInitialState(self, gameState):
        CaptureAgent.registerInitialState(self, gameState)

        self.walls = gameState.getWalls()
        self.startPos = gameState.getAgentPosition(self.index)

    def chooseAction(self, gameState):

        max_depth = 4

        # max agent (Pacman)
        def max_value(state, current_depth: int, agent_index: int, alpha, beta):

            # winning state or losing state -> just return the value
            if state.isOver() or current_depth == max_depth:
                return evaluate(state), "stop"

            # keep maximum value and the action to reach that value
            v = -999999999  # -INF
            nextAction = ""
            actions = state.getLegalActions(agent_index)

            # explore next min nodes
            for action in actions:
                successorState = state.generateSuccessor(agent_index, action)
                nextValue = min_value(successorState, current_depth + 1, (agent_index + 1) % 4, alpha, beta)[0]
                if nextValue > v:
                    v = nextValue
                    nextAction = action

                # if v > beta, the before min node won't choose this path. -> pruning
                if v > beta:
                    return v, nextAction

                # update alpha to maximum value
                alpha = max(alpha, v)

            if state.getAgentPosition(agent_index) == self.startPos:
                return -99999, nextAction

            return v, nextAction

        # min agent (Ghosts)
        def min_value(state, current_depth: int, agent_index: int, alpha, beta):

            # winning state or losing state -> just return the value
            if state.isOver() or current_depth >= max_depth:
                return evaluate(state), "Stop"

            # keep minimum value
            v = 999999999  # +INF
            nextAction = ""
            actions = state.getLegalActions(agent_index)

            for action in actions:
                successorState = state.generateSuccessor(agent_index, action)
                nextValue = max_value(successorState, current_depth + 1, (agent_index + 1) % 4, alpha, beta)[0]
                if nextValue < v:
                    v = nextValue
                    nextAction = action

                v = min(v, nextValue)

                # if v < alpha, the before max node won't choose this path. -> pruning
                if v < alpha:
                    return v, nextAction

                # update beta to minimum value
                beta = min(beta, v)

            return v, nextAction

        def evaluate(state):

            opponents = self.getOpponents(state)
            ret = self.index

            my_agent_state = state.getAgentState(self.index)
            my_agent_pos = state.getAgentPosition(self.index)

            for j in opponents:
                op_agent_state = state.getAgentState(j)

                if my_agent_state.isPacman:
                    if not op_agent_state.isPacman:
                        ret += min(self.getMazeDistance(my_agent_pos, state.getAgentPosition(j)),
                                   3) * 3
                else:
                    if op_agent_state.isPacman:
                        ret -= min(self.getMazeDistance(my_agent_pos, state.getAgentPosition(j)), 5)

            ret += my_agent_state.numCarrying * 20
            ret += my_agent_state.numReturned * 15

            if self.index % 2 == 0:  # red team
                foods = state.getBlueFood()
            else:
                foods = state.getRedFood()

            ret -= nearest_food(my_agent_pos[0], my_agent_pos[1], foods)

            print(ret)
            return ret

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

        return max_value(gameState, 0, self.index, -999999999, 999999999)[1]  # initial alpha/beta value : -INF/+INF
