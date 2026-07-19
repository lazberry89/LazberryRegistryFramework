package Framework.LazberryRegistryFramework;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * <h2>LRF Structural Dependency Graph Logger</h2>
 *
 * <h3>1. Motivation & Aesthetic Diagnostics</h3>
 * As a recursive dependency injection graph scales, tracking the exact assembly sequence and identifying the root cause
 * of initialization bottlenecks or failures becomes increasingly difficult. Standard linear logs fail to capture the hierarchical
 * depth of object graphs.
 * <p>
 * {@code StructuralLog} provides <b>tree-structured visualization logs</b> and <b>isolated failure path traces</b>. It translates
 * complex recursive reflection sequences into human-readable console graphics, allowing developers to instantly verify the
 * assembly status, component dependency count, and custom framework lifecycle invocation states.
 *
 * <h3>2. Operational Design & Indentation Math</h3>
 * The visual rendering engine dynamically calculates space indentation based on the absolute traversal depth within the
 * {@link DependencyContainer#constructionStack()}.
 * <ul>
 * <li><b>Root Nodes (Depth = 0):</b> Flagged with a distinct starting token (e.g., {@code 🟢 [Root]}) to mark the primary entry point.</li>
 * <li><b>Child Nodes (Depth > 0):</b> Indented via mathematical repeating blocks ({@code " "}) and branch arrows ({@code └──>}) to map
 * direct structural inheritance or composition pipelines.</li>
 * </ul>
 *
 * <h3>3. Failure Isolation & Graph Diagnostics</h3>
 * When a nested bean assembly sequence collapses due to missing configurations or structural errors, the component invokes
 * {@link #logAssemblyFailure(Set, Class, Throwable)}. It takes a snapshot of the historical execution stack and flushes an isolated
 * diagnostic block to the console. This isolates the exact vector of failure, showing the precisely formatted path from the
 * top-level entry point straight down to the breaking component.
 *
 * <h3>4. Sample Output Blueprint</h3>
 * <pre>
 * 🟢 [Root] DungeonManager
 * └──> [Needs] SqlManager
 * ├─ Perfect Assembly: SqlManager (with 0 deps)
 * ├─ Perfect Assembly: DungeonManager (with 1 dep)
 * └─ [LRF-Lifecycle] Callback Success: DungeonManager
 * </pre>
 *
 * @author Lazberry (LRF Architecture Team)
 * @version 1.2.9
 * @see DependencyContainer
 * @see PackageScanner
 */
@Slf4j
public final class StructuralLog {
    private static final @NotNull String ICON = LazberryRegistryFramework.icon(false);

    /**
     * Renders the starting entry visual branch of a targeted class inside the current dependency chain resolution loop.
     *
     * @param depth The current recursive resolution depth level within the dependency allocation frame.
     * @param clazz The structural class metadata currently undergoing container analysis.
     */
    public static void logDependencyStart(int depth, @NotNull Class<?> clazz) {
        if (!LazberryRegistryFramework.isLogDrawStructure()) return;

        String indent = "    ".repeat(depth);
        String arrow = depth > 0 ? " └──> [Needs] " : " 🟢 [Root] ";
        log.info("{}{}{}{}", ICON, indent, arrow, clazz.getSimpleName());
    }

    /**
     * Renders a success milestone log indicating a constructor reflection sequence completed perfectly with all parameter inputs fulfilled.
     *
     * @param depth    The structural depth mapping location of the assembled target component.
     * @param clazz    The concrete class signature that successfully initialized into a singleton bean.
     * @param depCount The absolute total count of arguments resolved and injected into the constructor signature.
     */
    public static void logAssemblySuccess(int depth, @NotNull Class<?> clazz, int depCount) {
        if (!LazberryRegistryFramework.isLogDrawStructure()) return;

        String indent = "    ".repeat(depth);
        log.info("{}{} ├─ Perfect Assembly: {} (with {} deps)", ICON, indent, clazz.getSimpleName(), depCount);
    }

    /**
     * Renders a completion message verifying that the target instance's custom initialization lifecycles were successfully invoked.
     *
     * @param depth The final depth state tracking location of the processed instance.
     * @param clazz The structural target class that finished its lifecycle validation hooks.
     */
    public static void logLifecycleSuccess(int depth, @NotNull Class<?> clazz) {
        if (!LazberryRegistryFramework.isLogDrawStructure()) return;

        String indent = "    ".repeat(depth);
        log.info("{}{} └─ [LRF-Lifecycle] Callback Success: {}", ICON, indent, clazz.getSimpleName());
    }

    /**
     * Flushes a highly formatted diagnostic failure report to the console error block when a dependency graph sequence collapses.
     * <p>
     * This utility loops through the isolated failure stack to dump an absolute structural breadcrumb trace leading directly to the exception source.
     *
     * @param failureStack An ordered historical tracking collection representing the active execution context path at the moment of the crash.
     * @param rootClass    The top-level component that initiated the broken initialization sequence.
     * @param cause        The precise underlying runtime exception instance responsible for the assembly disruption.
     */
    public static void logAssemblyFailure(@NotNull Set<Class<?>> failureStack, @NotNull Class<?> rootClass, @NotNull Throwable cause) {
        log.error("=========================================================");
        log.error("{} ❌ {} Dependency Assembly Stopped!", ICON, LazberryRegistryFramework.failureIcon());
        log.error("{} Root Target: {}", ICON, rootClass.getSimpleName());
        log.error("{} Reason: {}", ICON, cause.getMessage());
        log.error("{} -----------------------------------------------------", ICON);
        log.error("{} [Execution Path Trace]:", ICON);

        int depth = 0;
        for (Class<?> clazz : failureStack) {
            String indent = "    ".repeat(depth++);
            log.error("{}{} └── [X] {}", ICON, indent, clazz.getSimpleName());
        }
        log.error("=========================================================");
    }
}