# multiAgents.py
# --------------
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


from util import manhattanDistance
from game import Directions
import random, util

from game import Agent


class ReflexAgent(Agent):
    """
    A reflex agent chooses an action at each choice point by examining
    its alternatives via a state evaluation function.

    The code below is provided as a guide.  You are welcome to change
    it in any way you see fit, so long as you don't touch our method
    headers.
    """

    def getAction(self, gameState):
        """
        You do not need to change this method, but you're welcome to.

        getAction chooses among the best options according to the evaluation function.

        Just like in the previous project, getAction takes a GameState and returns
        some Directions.X for some X in the set {NORTH, SOUTH, WEST, EAST, STOP}
        """
        # Collect legal moves and successor states
        legalMoves = gameState.getLegalActions()

        # Choose one of the best actions
        scores = [self.evaluationFunction(gameState, action) for action in legalMoves]
        bestScore = max(scores)
        bestIndices = [index for index in range(len(scores)) if scores[index] == bestScore]
        chosenIndex = random.choice(bestIndices)  # Pick randomly among the best

        "Add more of your code here if you want to"

        return legalMoves[chosenIndex]

    def evaluationFunction(self, currentGameState, action):
        """
        Design a better evaluation function here.

        The evaluation function takes in the current and proposed successor
        GameStates (pacman.py) and returns a number, where higher numbers are better.

        The code below extracts some useful information from the state, like the
        remaining food (newFood) and Pacman position after moving (newPos).
        newScaredTimes holds the number of moves that each ghost will remain
        scared because of Pacman having eaten a power pellet.

        Print out these variables to see what you're getting, then combine them
        to create a masterful evaluation function.
        """
        # Useful information you can extract from a GameState (pacman.py)
        successorGameState = currentGameState.generatePacmanSuccessor(action)
        newPos = successorGameState.getPacmanPosition()
        newFood = successorGameState.getFood()
        newGhostStates = successorGameState.getGhostStates()
        newScaredTimes = [ghostState.scaredTimer for ghostState in newGhostStates]

        "*** YOUR CODE HERE ***"

        # find the actual distance between Pacman and the nearest food
        def nearest_food(x, y):
            q = util.Queue()
            dist = 1
            q.push((x, y))
            visited = [[False for _y in range(newFood.height)] for _x in range(newFood.width)]
            visited[x][y] = True
            dx = [1, -1, 0, 0]
            dy = [0, 0, 1, -1]
            while not q.isEmpty():
                for qSize in range(len(q.list)):
                    cur = q.pop()
                    if newFood[cur[0]][cur[1]]:
                        return dist
                    for d in range(4):
                        nx = cur[0] + dx[d]
                        ny = cur[1] + dy[d]
                        if 0 <= nx < newFood.width and 0 <= ny < newFood.height:
                            if not visited[nx][ny]:
                                visited[nx][ny] = True
                                q.push((nx, ny))
                dist += 1

        # successor is winning state
        if successorGameState.getNumFood() == 0:
            return 500
        distanceToGhosts = [manhattanDistance(newPos, newGhostState.getPosition()) for newGhostState in newGhostStates]
        distanceToNearestGhost = min(distanceToGhosts + [2])  # if ghost is farther than 2 steps, don't mind the ghost
        distanceToNearestFood = nearest_food(newPos[0], newPos[1])

        # farther to the ghost is better. farther to the food is worse.
        score = distanceToNearestGhost / distanceToNearestFood + (
                    successorGameState.getScore() - currentGameState.getScore())

        return score


def scoreEvaluationFunction(currentGameState):
    """
    This default evaluation function just returns the score of the state.
    The score is the same one displayed in the Pacman GUI.

    This evaluation function is meant for use with adversarial search agents
    (not reflex agents).
    """
    return currentGameState.getScore()


class MultiAgentSearchAgent(Agent):
    """
    This class provides some common elements to all of your
    multi-agent searchers.  Any methods defined here will be available
    to the MinimaxPacmanAgent, AlphaBetaPacmanAgent & ExpectimaxPacmanAgent.

    You *do not* need to make any changes here, but you can if you want to
    add functionality to all your adversarial search agents.  Please do not
    remove anything, however.

    Note: this is an abstract class: one that should not be instantiated.  It's
    only partially specified, and designed to be extended.  Agent (game.py)
    is another abstract class.
    """

    def __init__(self, evalFn='scoreEvaluationFunction', depth='2'):
        self.index = 0  # Pacman is always agent index 0
        self.evaluationFunction = util.lookup(evalFn, globals())
        self.depth = int(depth)


class MinimaxAgent(MultiAgentSearchAgent):
    """
    Your minimax agent (question 2)
    """

    def getAction(self, gameState):
        """
        Returns the minimax action from the current gameState using self.depth
        and self.evaluationFunction.

        Here are some method calls that might be useful when implementing minimax.

        gameState.getLegalActions(agentIndex):
        Returns a list of legal actions for an agent
        agentIndex=0 means Pacman, ghosts are >= 1

        gameState.generateSuccessor(agentIndex, action):
        Returns the successor game state after an agent takes an action

        gameState.getNumAgents():
        Returns the total number of agents in the game

        gameState.isWin():
        Returns whether or not the game state is a winning state

        gameState.isLose():
        Returns whether or not the game state is a losing state
        """
        "*** YOUR CODE HERE ***"

        # max agent (Pacman)
        def max_value(state, current_depth: int):

            # winning state or losing state -> just return the value
            if state.isWin() or state.isLose() or current_depth == self.depth:
                return self.evaluationFunction(state), "stop"

            # keep maximum value and the action to reach that value
            v = -999999999  # -INF
            nextAction = ""
            actions = state.getLegalActions(0)

            # explore next min nodes
            for action in actions:
                successorState = state.generateSuccessor(0, action)
                nextValue = min_value(successorState, current_depth + 1, 1)
                if nextValue > v:
                    v = nextValue
                    nextAction = action

            return v, nextAction

        # min agent (Ghosts)
        def min_value(state, current_depth: int, agent_index: int):

            # winning state or losing state -> just return the value
            if state.isWin() or state.isLose():
                return self.evaluationFunction(state)

            # keep minimum value
            v = 999999999  # +INF

            actions = state.getLegalActions(agent_index)

            for action in actions:
                successorState = state.generateSuccessor(agent_index, action)

                # Check if it is the last ghost to move
                if agent_index == state.getNumAgents() - 1:
                    nextValue = max_value(successorState, current_depth)[0]  # next move is Pacman
                else:
                    nextValue = min_value(successorState, current_depth, agent_index + 1)  # next ghost moves

                v = min(v, nextValue)

            return v

        return max_value(gameState, 0)[1]


class AlphaBetaAgent(MultiAgentSearchAgent):
    """
    Your minimax agent with alpha-beta pruning (question 3)
    """

    def getAction(self, gameState):

        # max agent (Pacman)
        def max_value(state, current_depth: int, alpha, beta):

            # winning state or losing state -> just return the value
            if state.isWin() or state.isLose() or current_depth == self.depth:
                return self.evaluationFunction(state), "stop"

            # keep maximum value and the action to reach that value
            v = -999999999  # -INF
            nextAction = ""
            actions = state.getLegalActions(0)

            # explore next min nodes
            for action in actions:
                successorState = state.generateSuccessor(0, action)
                nextValue = min_value(successorState, current_depth + 1, 1, alpha, beta)
                if nextValue > v:
                    v = nextValue
                    nextAction = action

                # if v > beta, the before min node won't choose this path. -> pruning
                if v > beta:
                    return v, nextAction

                # update alpha to maximum value
                alpha = max(alpha, v)

            return v, nextAction

        # min agent (Ghosts)
        def min_value(state, current_depth: int, agent_index: int, alpha, beta):

            # winning state or losing state -> just return the value
            if state.isWin() or state.isLose():
                return self.evaluationFunction(state)

            # keep minimum value
            v = 999999999  # +INF

            actions = state.getLegalActions(agent_index)

            for action in actions:
                successorState = state.generateSuccessor(agent_index, action)

                # Check if it is the last ghost to move
                if agent_index == state.getNumAgents() - 1:
                    nextValue = max_value(successorState, current_depth, alpha, beta)[0]  # next move is Pacman
                else:
                    nextValue = min_value(successorState, current_depth, agent_index + 1, alpha, beta)  # next ghost moves

                v = min(v, nextValue)

                # if v < alpha, the before max node won't choose this path. -> pruning
                if v < alpha:
                    return v

                # update beta to minimum value
                beta = min(beta, v)

            return v

        return max_value(gameState, 0, -999999999, 999999999)[1]  # initial alpha/beta value : -INF/+INF


class ExpectimaxAgent(MultiAgentSearchAgent):
    """
      Your expectimax agent (question 4)
    """

    def getAction(self, gameState):
        """
        Returns the expectimax action using self.depth and self.evaluationFunction

        All ghosts should be modeled as choosing uniformly at random from their
        legal moves.
        """
        "*** YOUR CODE HERE ***"
        util.raiseNotDefined()


def betterEvaluationFunction(currentGameState):
    """
    Your extreme ghost-hunting, pellet-nabbing, food-gobbling, unstoppable
    evaluation function (question 5).

    DESCRIPTION: <write something here so we know what you did>
    """
    "*** YOUR CODE HERE ***"
    util.raiseNotDefined()


# Abbreviation
better = betterEvaluationFunction
