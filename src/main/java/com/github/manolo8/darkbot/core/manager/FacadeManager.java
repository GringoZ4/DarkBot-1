package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.facades.AstralGateProxy;
import com.github.manolo8.darkbot.core.objects.facades.BoosterProxy;
import com.github.manolo8.darkbot.core.objects.facades.ChatProxy;
import com.github.manolo8.darkbot.core.objects.facades.ChrominProxy;
import com.github.manolo8.darkbot.core.objects.facades.EscortProxy;
import com.github.manolo8.darkbot.core.objects.facades.EternalBlacklightProxy;
import com.github.manolo8.darkbot.core.objects.facades.EternalGateProxy;
import com.github.manolo8.darkbot.core.objects.facades.FrozenLabyrinthProxy;
import com.github.manolo8.darkbot.core.objects.facades.HighlightProxy;
import com.github.manolo8.darkbot.core.objects.facades.LogMediator;
import com.github.manolo8.darkbot.core.objects.facades.SettingsProxy;
import com.github.manolo8.darkbot.core.objects.facades.SlotBarsProxy;
import com.github.manolo8.darkbot.core.objects.facades.SpaceMapWindowProxy;
import com.github.manolo8.darkbot.core.objects.facades.StatsProxy;
import com.github.manolo8.darkbot.core.objects.swf.PairArray;
import eu.darkbot.api.PluginAPI;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class FacadeManager implements Manager, eu.darkbot.api.API.Singleton {
    private final PairArray commands         = PairArray.ofArray();
    private final PairArray proxies          = PairArray.ofArray();
    private final PairArray mediators        = PairArray.ofArray();
    private final List<Updatable> updatables = new ArrayList<>();

    private final PluginAPI pluginAPI;

    public final LogMediator log;
    public final ChatProxy chat;
    public final StatsProxy stats;
    public final EscortProxy escort;
    public final BoosterProxy booster;
    public final SettingsProxy settings;
    public final SlotBarsProxy slotBars;
    public final FrozenLabyrinthProxy labyrinth;
    public final EternalGateProxy eternalGate;
    public final EternalBlacklightProxy blacklightGate;
    public final ChrominProxy chrominEvent;
    public final AstralGateProxy astralGate;
    public final HighlightProxy highlight;
    public final SpaceMapWindowProxy spaceMapWindowProxy;

    public FacadeManager(PluginAPI pluginApi) {
        this.pluginAPI = pluginApi;

        this.log            = registerMediator("LogWindowMediator",   LogMediator.class);
        this.chat           = registerProxy("ChatProxy",              ChatProxy.class);
        this.stats          = registerProxy("StatsProxy",             StatsProxy.class);
        this.escort         = registerProxy("payload_escort",         EscortProxy.class);
        this.booster        = registerProxy("BoosterProxy",           BoosterProxy.class);
        this.settings       = registerProxy("SettingsWindowFUIProxy", SettingsProxy.class);
        this.slotBars       = registerProxy("ItemsControlMenuProxy",  SlotBarsProxy.class);
        this.labyrinth      = registerProxy("frozen_labyrinth",       FrozenLabyrinthProxy.class);
        this.eternalGate    = registerProxy("eternal_gate",           EternalGateProxy.class);
        this.blacklightGate = registerProxy("eternal_blacklight",     EternalBlacklightProxy.class);
        this.chrominEvent   = registerProxy("chrominEvent",           ChrominProxy.class);
        this.astralGate     = registerProxy("rogue_lite",             AstralGateProxy.class);
        this.highlight      = registerProxy("HighlightProxy",         HighlightProxy.class);
        this.spaceMapWindowProxy = registerProxy("spacemap",     SpaceMapWindowProxy.class);
    }

    public <T extends Updatable> T registerCommand(String key, Class<T> commandClass) {
        T command = pluginAPI.requireInstance(commandClass);
        this.commands.addLazy(key, ((Updatable) command)::update);
        updatables.add(command);
        return command;
    }

    public <T extends Updatable> T registerProxy(String key, Class<T> proxyClass) {
        T proxy = pluginAPI.requireInstance(proxyClass);
        this.proxies.addLazy(key, ((Updatable) proxy)::update);
        this.updatables.add(proxy);
        return proxy;
    }

    public <T extends Updatable> T registerMediator(String key, Class<T> mediatorClass) {
        T mediator = pluginAPI.requireInstance(mediatorClass);
        this.mediators.addLazy(key, ((Updatable) mediator)::update);
        updatables.add(mediator);
        return mediator;
    }

    public long getProxyAddressOf(String key) {
        return proxies.getPtr(key);
    }

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.mainAddress.add(mainAddr -> {
            long facade = API.readMemoryLong(mainAddr + 544);

            commands.update(API.readMemoryLong(facade, 0x28, 0x20));
            proxies.update(API.readMemoryLong(facade, 0x38, 0x30));
            mediators.update(API.readMemoryLong(facade, 0x40, 0x38));
        });
    }

    public void tick() {
        // Currently commands are not used by the bot and they represent
        // a decently big cpu chunk in ticking. Leaving them out reduces tick time significantly.
        //commands.update();
        proxies.update();
        mediators.update();

        for (Updatable updatable : updatables) {
            if (updatable.address != 0) updatable.update();
        }
    }
}
