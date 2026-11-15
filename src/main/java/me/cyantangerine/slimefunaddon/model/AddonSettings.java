package me.cyantangerine.slimefunaddon.model;

import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;

/**
 * 全部可调参数的唯一入口：值来自插件数据目录的 {@code config.yml}。
 *
 * <p>插件启动时读取一次并缓存为不可变字段，运行期只读，Folia 下任意区域 / 实体线程
 * 都可安全访问。业务类里不再保留任何调优用的魔法数字 —— 需要调整行为只改
 * {@code config.yml} 并重启，不要改 Java 源码。</p>
 *
 * <p>若配置文件里缺某个键（例如从旧版本升级），{@link Config#getOrSetDefault(String, Object)}
 * 会退回内置默认值并在内存中补上该键；随插件发布的 config.yml 已包含全部键。</p>
 *
 * <p>结构性常量（GUI 格数与槽位、分页布局、物品 ID、PDC 键、材质、文案）不在此列，
 * 它们与界面布局 / 存档格式绑定，改成可配置会直接破坏代码。</p>
 */
public final class AddonSettings {

    private static volatile AddonSettings instance;

    public final boolean debug;
    public final boolean lockExternalTransfers;

    public final int networkMaxContainers;
    public final int networkMaxConnectors;
    public final long connectorParticlePeriodTicks;

    public final long interfaceRefreshPeriodTicks;
    public final int interfaceFullRediscoverEvery;

    public final int wirelessMaxNameLength;

    public final int backpackStackMultiplier;
    public final int coolerStackMultiplier;
    public final long creativeActionDedupMillis;
    public final long creativeLeakWindowMillis;
    public final long initWatchPeriodTicks;
    public final int initWatchMaxChecks;

    public final float coolerSaturationThreshold;
    public final float coolerSaturationRestore;
    public final long coolerFeedCooldownMillis;
    public final long coolerCheckIntervalTicks;
    public final int coolerLowJuiceWarningBelow;

    private AddonSettings(Config cfg) {
        debug = bool(cfg, "options.debug", true);
        lockExternalTransfers = bool(cfg, "options.smart-storage-lock-external-transfers", false);

        networkMaxContainers = positiveInt(cfg, "options.network.max-containers", 32);
        networkMaxConnectors = positiveInt(cfg, "options.network.max-connectors", 512);
        connectorParticlePeriodTicks = positiveLong(cfg, "options.network.connector-particle-period-ticks", 10);

        interfaceRefreshPeriodTicks = positiveLong(cfg, "options.interface.refresh-period-ticks", 20);
        interfaceFullRediscoverEvery = positiveInt(cfg, "options.interface.full-rediscover-every", 5);

        wirelessMaxNameLength = positiveInt(cfg, "options.wireless-terminal.max-name-length", 24);

        backpackStackMultiplier = positiveInt(cfg, "options.container.backpack-stack-multiplier", 1);
        coolerStackMultiplier = positiveInt(cfg, "options.container.cooler-stack-multiplier", 1);
        creativeActionDedupMillis = nonNegativeLong(cfg, "options.container.creative-action-dedup-millis", 250);
        creativeLeakWindowMillis = nonNegativeLong(cfg, "options.container.creative-leak-window-millis", 15_000);
        initWatchPeriodTicks = positiveLong(cfg, "options.container.init-watch-period-ticks", 10);
        initWatchMaxChecks = positiveInt(cfg, "options.container.init-watch-max-checks", 60);

        coolerSaturationThreshold = nonNegativeFloat(cfg, "options.cooler.saturation-threshold", 2.0);
        coolerSaturationRestore = nonNegativeFloat(cfg, "options.cooler.saturation-restore", 6.0);
        coolerFeedCooldownMillis = nonNegativeLong(cfg, "options.cooler.feed-cooldown-millis", 2_000);
        coolerCheckIntervalTicks = positiveLong(cfg, "options.cooler.check-interval-ticks", 10);
        coolerLowJuiceWarningBelow = nonNegativeInt(cfg, "options.cooler.low-juice-warning-below", 3);
    }

    /** 在 onEnable 最开头调用，必须早于任何物品注册 / 事件处理。 */
    public static void init(Config config) {
        instance = new AddonSettings(config);
    }

    public static AddonSettings get() {
        AddonSettings current = instance;
        if (current == null) {
            throw new IllegalStateException("AddonSettings 尚未初始化：请在 onEnable 中先调用 init(config)");
        }
        return current;
    }

    /** 调试开关：初始化之前一律返回 false，避免启动早期的静态调试调用抛异常。 */
    public static boolean isDebug() {
        AddonSettings current = instance;
        return current != null && current.debug;
    }

    // ---- 读取 helper：容忍 YAML 里写成 Integer / Long / Double，并对非法值做最小钳制 ----
    // 这里的数字只是「配置缺键时的兜底」，正常情况一律以 config.yml 为准。

    private static Object raw(Config cfg, String path, Object fallback) {
        return cfg.getOrSetDefault(path, fallback);
    }

    private static double number(Config cfg, String path, double fallback) {
        Object value = raw(cfg, path, fallback);
        return value instanceof Number n ? n.doubleValue() : fallback;
    }

    private static boolean bool(Config cfg, String path, boolean fallback) {
        Object value = raw(cfg, path, fallback);
        return value instanceof Boolean b ? b : fallback;
    }

    private static int positiveInt(Config cfg, String path, int fallback) {
        return Math.max(1, (int) number(cfg, path, fallback));
    }

    private static int nonNegativeInt(Config cfg, String path, int fallback) {
        return Math.max(0, (int) number(cfg, path, fallback));
    }

    private static long positiveLong(Config cfg, String path, long fallback) {
        return Math.max(1L, (long) number(cfg, path, fallback));
    }

    private static long nonNegativeLong(Config cfg, String path, long fallback) {
        return Math.max(0L, (long) number(cfg, path, fallback));
    }

    private static float nonNegativeFloat(Config cfg, String path, double fallback) {
        return Math.max(0.0F, (float) number(cfg, path, fallback));
    }
}
