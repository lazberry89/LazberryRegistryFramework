package Framework.Utils;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class that generates random id only with Numbers and Letters.
 */
public class IDGenerator {

	/**
	 * I like coding in legacy way. Instead of using {@link lombok.experimental.UtilityClass},
	 * I prefer making Constructor untouchable.
	 */
	@ApiStatus.Internal
	@Contract("-> fail")
	private IDGenerator() {
		throw new UnsupportedOperationException("Utility class");
	}

    private static final @NotNull String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	/**
	 * ID generating method. It generates totally random id.
	 * But, this method does not check for Duplicated cases.
	 * @param size how long the results be.
	 * @return String value of generated id
	 */
    public static @NotNull String generateRandomId(int size) {
        StringBuilder sb = new StringBuilder(size);

        for (int i = 0; i < size; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }

        return sb.toString();
    }
}
