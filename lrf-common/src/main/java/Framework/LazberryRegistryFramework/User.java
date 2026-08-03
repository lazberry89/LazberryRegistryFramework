package Framework.LazberryRegistryFramework;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface User<ID> {
	@NotNull ID getId();
	String getName();
	Optional<Data> getData();
}
