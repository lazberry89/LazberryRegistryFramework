package Framework;

/**
 * Only Used by Reflection annotation to divide process
 * e.g. @Reflection(InitializeType.COMMANDS)
 */
public enum InitializeType {
	LISTENERS,
	COMMANDS,
	TASKS_ON,
	TASKS_OFF,
	REGISTER,
	NETWORKS,
	EXCEPTED,
}
