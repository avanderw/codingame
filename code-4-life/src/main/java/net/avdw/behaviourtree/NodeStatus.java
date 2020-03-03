package net.avdw.behaviourtree;

public enum NodeStatus {
    /** returned when a criterion has been met by a condition node or an action node has been completed successfully */
    SUCCESS,
    /** returned when a criterion has not been met by a condition node or an action node could not finish its execution for any reason */
    FAILURE,
    /** returned when an action node has been initialized but is still waiting for its resolution */
    RUNNING
}
