package Framework.Utils;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * <h2>SubCommand (Decoupled Command Pattern Boundary Specification)</h2>
 * <p>
 * Represents a functional behavioral contract for isolating localized sub-command actions
 * within a unified primary command ecosystem (e.g., separating {@code /plot create} and {@code /plot trust}).
 * </p>
 * <h3>Architectural Philosophy & Anti-Pattern Mitigation:</h3>
 * <p>
 * Traditional Bukkit development heavily relies on monolithic {@code onCommand} blocks riddled with nested,
 * unmaintainable {@code if-else} or {@code switch} string-matching routines. This interface mitigates that
 Monolithic antipattern by enforcing the <b>Command Pattern</b>, encapsulating each unique action
 * into its own single-responsibility object.
 * </p>
 * <h3>IoC Container Lifecycle & Routing Mechanics:</h3>
 * <ul>
 * <li><b>Auto-Scanning:</b> Framework components implementing this interface and annotated with LRF
 * command identifiers are automatically detected by the {@link Framework.LazberryRegistryFramework.PackageScanner}.</li>
 * <li><b>Dynamic Multiplexing:</b> The primary command executor holds a mapped infrastructure of these
 * {@code SubCommand} beans (typically {@code Map<String, SubCommand>}). When a player fires an input,
 * the executor strips the root argument, matches the sub-key, and forwards the context downstream instantly.</li>
 * </ul>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see org.bukkit.command.CommandExecutor
 */
public interface SubCommand {

	/**
	 * Executes the encapsulated contextual business logic assigned to this specific sub-command branch.
	 * <p>
	 * <b>Defensive Parsing Guarantee:</b>
	 * The argument array passed into this boundary is structurally pre-processed by the primary routing executor.
	 * The token used to trigger this sub-command (e.g., the keyword "create") is safely stripped out, meaning the
	 * {@code args} parameter contains only the trailing parameters dedicated strictly to this execution scope.
	 * </p>
	 *
	 * @param player The guaranteed non-null native Bukkit {@link Player} instance who explicitly triggered this transaction.
	 * @param args   The non-null, pre-filtered subsequent command string tokens accompanying the execution context.
	 */
    void execute(@NotNull Player player, @NotNull String @NotNull...args);
}
