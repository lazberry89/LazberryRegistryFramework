package Framework;

import Framework.Annotation.ConsumableClass;
import org.jetbrains.annotations.NotNull;

@ConsumableClass
public class Global implements ServerType {
	private boolean global = true;

	@Override
	public boolean requiresGlobalInitiator() {
		return this.global;
	}

	@Override
	public void setGlobalInitiatorRequired(boolean required) {
		this.global = required;
	}

	@Override
	public @NotNull String getName() {
		return "global";
	}
}
