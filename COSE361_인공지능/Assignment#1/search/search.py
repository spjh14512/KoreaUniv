# search.py
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


"""
In search.py, you will implement generic search algorithms which are called by
Pacman agents (in searchAgents.py).
"""

import util

class SearchProblem:
    """
    This class outlines the structure of a search problem, but doesn't implement
    any of the methods (in object-oriented terminology: an abstract class).

    You do not need to change anything in this class, ever.
    """

    def getStartState(self):
        """
        Returns the start state for the search problem.
        """
        util.raiseNotDefined()

    def isGoalState(self, state):
        """
          state: Search state

        Returns True if and only if the state is a valid goal state.
        """
        util.raiseNotDefined()

    def getSuccessors(self, state):
        """
          state: Search state

        For a given state, this should return a list of triples, (successor,
        action, stepCost), where 'successor' is a successor to the current
        state, 'action' is the action required to get there, and 'stepCost' is
        the incremental cost of expanding to that successor.
        """
        util.raiseNotDefined()

    def getCostOfActions(self, actions):
        """
         actions: A list of actions to take

        This method returns the total cost of a particular sequence of actions.
        The sequence must be composed of legal moves.
        """
        util.raiseNotDefined()


def tinyMazeSearch(problem):
    """
    Returns a sequence of moves that solves tinyMaze.  For any other maze, the
    sequence of moves will be incorrect, so only use this for tinyMaze.
    """
    from game import Directions
    s = Directions.SOUTH
    w = Directions.WEST
    return  [s, s, w, s, w, w, s, w]

def depthFirstSearch(problem):
    """
    Search the deepest nodes in the search tree first.

    Your search algorithm needs to return a list of actions that reaches the
    goal. Make sure to implement a graph search algorithm.

    To get started, you might want to try some of these simple commands to
    understand the search problem that is being passed in:

    print("Start:", problem.getStartState())
    print("Is the start a goal?", problem.isGoalState(problem.getStartState()))
    print("Start's successors:", problem.getSuccessors(problem.getStartState()))
    """
    "*** YOUR CODE HERE ***"
    st = util.Stack()
    startState = problem.getStartState()
    st.push((startState, 'Stop', 0))
    vis = set() # visited
    parent = dict() # for back tracing

    while True:
        if st.isEmpty():
            return []
        currentSuccessor = st.pop()
        currentState = currentSuccessor[0]
        vis.add(currentState)
        if problem.isGoalState(currentState):
            break
        successors = problem.getSuccessors(currentState)
        unvis_successors = list(filter(lambda x: x[0] not in vis, successors))
        for successor in unvis_successors:
            st.push(successor)
            parent[successor[0]] = (currentState, successor[1])

    actions = []
    while currentState != startState:
        back = parent[currentState]
        actions.append(back[1])
        currentState = back[0]

    return list(reversed(actions))

    # util.raiseNotDefined()

def breadthFirstSearch(problem):
    """Search the shallowest nodes in the search tree first."""
    "*** YOUR CODE HERE ***"
    queue = util.Queue()
    startState = problem.getStartState()
    queue.push((startState, 'Stop', 0))
    vis = set()
    vis.add(startState)
    parent = dict()

    while True:
        if queue.isEmpty():
            return []
        currentSuccessor = queue.pop()
        currentState = currentSuccessor[0]
        if problem.isGoalState(currentState):
            break
        successors = problem.getSuccessors(currentState)
        unvis_successors = list(filter(lambda x: x[0] not in vis, successors))
        for successor in unvis_successors:
            vis.add(successor[0])
            queue.push(successor)
            parent[successor[0]] = (currentState, successor[1])

    actions = []
    while currentState != startState:
        back = parent[currentState]
        actions.append(back[1])
        currentState = back[0]

    return list(reversed(actions))

    # util.raiseNotDefined()

def uniformCostSearch(problem):
    """Search the node of least total cost first."""
    "*** YOUR CODE HERE ***"
    pq = util.PriorityQueueWithFunction(lambda x: x[1]) # (State, Dist, Path)
    sure = set()

    startState = problem.getStartState()
    pq.push((startState, 0, [])) # (State, Dist, Path)

    while True:
        if pq.isEmpty():
            return []
        pqTop = pq.pop()
        currentState = pqTop[0]
        currentDist = pqTop[1]
        currentPath = pqTop[2]

        if currentState in sure:
            continue
        sure.add(currentState)
        if problem.isGoalState(currentState):
            return currentPath
        successors = problem.getSuccessors(currentState)
        unsure_successors = list(filter(lambda x: x[0] not in sure, successors))
        for successor in unsure_successors:
            nextState, action, edgeCost = successor
            pq.push((nextState, currentDist + edgeCost, currentPath + [action]))

    # util.raiseNotDefined()

def nullHeuristic(state, problem=None):
    """
    A heuristic function estimates the cost from the current state to the nearest
    goal in the provided SearchProblem.  This heuristic is trivial.
    """
    return 0

def aStarSearch(problem, heuristic=nullHeuristic):
    """Search the node that has the lowest combined cost and heuristic first."""
    "*** YOUR CODE HERE ***"
    pq = util.PriorityQueueWithFunction(lambda x: x[3])  # (State, Dist, Path, f(n))
    sure = set()

    startState = problem.getStartState()
    pq.push((startState, 0, [], heuristic(startState, problem)))  # (State, Dist, Path)

    while True:
        if pq.isEmpty():
            return []
        pqTop = pq.pop()
        currentState = pqTop[0]
        currentDist = pqTop[1]
        currentPath = pqTop[2]

        if currentState in sure:
            continue
        sure.add(currentState)
        if problem.isGoalState(currentState):
            return currentPath
        successors = problem.getSuccessors(currentState)
        unsure_successors = list(filter(lambda x: x[0] not in sure, successors))
        for successor in unsure_successors:
            nextState, action, edgeCost = successor
            pq.push((nextState, currentDist + edgeCost, currentPath + [action], currentDist + edgeCost + heuristic(nextState, problem)))

    # util.raiseNotDefined()


# Abbreviations
bfs = breadthFirstSearch
dfs = depthFirstSearch
astar = aStarSearch
ucs = uniformCostSearch
