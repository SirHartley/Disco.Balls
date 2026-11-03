package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;
import disco.scripts.DiscoBallScript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MoreColour_DiscoControls extends BaseCommandPlugin {

    public static final String DISCO_MODE_ENGAGED = "$isDiscoMode";

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        String action = params.get(0).getString(memoryMap);
        SectorEntityToken lamp = dialog.getInteractionTarget();

        if("ON".equals(action)){
            memoryMap.get(MemKeys.LOCAL).set(DISCO_MODE_ENGAGED, true);
            Global.getSector().addScript(new DiscoBallScript(lamp.getOrbitFocus().getMarket()));
        } else if ("OFF".equals(action)) {
            memoryMap.get(MemKeys.LOCAL).set(DISCO_MODE_ENGAGED, false);

            for (EveryFrameScript script : new ArrayList<>(Global.getSector().getScripts())){
                if(script instanceof DiscoBallScript){
                    DiscoBallScript discoBallScript = ((DiscoBallScript) script);
                    if (discoBallScript.market.getId().equals(lamp.getOrbitFocus().getMarket().getId())) {
                        discoBallScript.setDone(true);
                    }
                }
            }
        }

        return true;
    }
}