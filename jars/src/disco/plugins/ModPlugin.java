package disco.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import disco.scripts.DiscoBallScript;
import disco.scripts.KantaLampInterceptorScript;

public class ModPlugin extends BaseModPlugin {
    @Override
    public void onGameLoad(boolean newGame) {
        super.onGameLoad(newGame);
        Global.getSector().addTransientScript(new KantaLampInterceptorScript());
    }

    @Override
    public void afterGameSave() {
        for (EveryFrameScript script : Global.getSector().getScripts()){
            if (script instanceof DiscoBallScript){
                ((DiscoBallScript) script).setMuted(false);
            }
        }

        super.afterGameSave();
    }

    @Override
    public void beforeGameSave() {
        for (EveryFrameScript script : Global.getSector().getScripts()){
            if (script instanceof DiscoBallScript){
                ((DiscoBallScript) script).setMuted(true);
            }
        }

        super.beforeGameSave();
    }
}
