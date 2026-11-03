package disco.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.KantaCMD;

/**
 * this is wasteful and dumb but it's a meme so who cares
 */

public class KantaLampInterceptorScript implements EveryFrameScript {

    public boolean done = false;

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        MarketAPI m = Global.getSector().getEconomy().getMarket("kantas_den");

        if (m == null) return;
        SectorEntityToken t = m.getPrimaryEntity();

        if (t.hasScriptOfClass(DiscoBallScript.class)) {
            done = true;
            return;
        }

        if (t.hasScriptOfClass(KantaCMD.FusionLampColorChanger.class)) {
            t.removeScriptsOfClass(KantaCMD.FusionLampColorChanger.class);
            t.addScript(new DiscoBallScript(m));
            done = true;
        }
    }
}
