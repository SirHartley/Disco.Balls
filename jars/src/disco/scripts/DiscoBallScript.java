package disco.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModPlugin;
import com.fs.starfarer.api.SoundAPI;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class DiscoBallScript extends BaseCampaignEventListenerAndScript {

    public static final List<Color> colourList =  new LinkedList<Color>(){{
        add(new Color(100, 255, 237, 255));
        add(new Color(255, 145, 253, 255));
        add(new Color(255, 50, 50, 255));
        add(new Color(210, 230, 255, 255));
        add(new Color(183, 50, 255, 255));
        add(new Color(255, 225, 125, 255));
        add(new Color(62, 150, 62, 255));
        add(new Color(46, 60, 255, 255));
        add(new Color(245, 250, 255, 255));
        add(new Color(100, 255, 100, 255));
        add(new Color(255, 90, 50, 255));
        add(new Color(255, 175, 94, 255));
        add(new Color(244, 255, 94, 255));
    }};

    public MarketAPI market;
    private WeightedRandomPicker<Color> picker = new WeightedRandomPicker<>();
    private transient SoundAPI caramelDansen;
    public boolean done = false;
    private boolean isMuted = false;

    float count = 0;
    float total = 0;

    public DiscoBallScript(MarketAPI market) {
        super();
        this.market = market;
    }

    @Override
    public boolean isDone() {
        return done || market.getContainingLocation() == null;
    }

    public void stopDansen(){
        if(caramelDansen != null) caramelDansen.stop();
        caramelDansen = null;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }

    public void setVolumeZero(){
        if(caramelDansen != null) caramelDansen.setVolume(0f);
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        if(done || market == null || market.getContainingLocation() == null){
            stopDansen();
            return;
        }

        playDansen(amount);
    }

    public void setDone(boolean done) {
        this.done = done;

        if (done){
            Global.getLogger(DiscoBallScript.class).info("deleting dansen");

            stopDansen();
            Global.getSector().removeListener(this);
        }
    }

    public void playDansen(float amount){
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        SectorAPI sector = Global.getSector();

        if (sector.isPaused()
                || isMuted
                || market.getContainingLocation() != playerFleet.getContainingLocation()
                || sector.getCampaignUI().isShowingDialog()
                || sector.getCampaignUI().isShowingMenu()
                || sector.getCampaignUI().getCurrentInteractionDialog() != null) {

            setVolumeZero();
            total += getAdjustedAmt(amount);
            return;
        }

        SectorEntityToken lamp = null;
        for (CustomCampaignEntityAPI curr : market.getContainingLocation().getCustomEntities()) {
            if (curr.getCustomEntityType().equals(Entities.FUSION_LAMP) &&
                    curr.getOrbitFocus() == market.getPrimaryEntity()) {
                lamp = curr;
                break;
            }
        }

        if (lamp == null) {
            setDone(true);
            return;
        }

        float vol = Math.min(1f, 250f / Misc.getDistance(playerFleet, lamp));

        if (caramelDansen == null || total > 178f) {
            total = 0;
            caramelDansen = Global.getSoundPlayer().playSound("carameldansen", 1f, vol, lamp.getLocation(), Misc.ZERO);
        }

        caramelDansen.setVolume(vol);
        caramelDansen.setLocation(lamp.getLocation().x, lamp.getLocation().y);

        float add = getAdjustedAmt(amount);
        count += add;
        total += add;

        if (count > 0.365f) {
            if (picker.isEmpty()) picker.addAll(colourList);

            MemoryAPI mem = lamp.getMemoryWithoutUpdate();
            Color nextColor = picker.pickAndRemove();

            mem.set("$core_lampGlowColor", nextColor);
            mem.set("$core_lampLightColor", nextColor);

            count = 0;
        }
    }

    @Override
    public void reportFleetJumped(CampaignFleetAPI fleet, SectorEntityToken from, JumpPointAPI.JumpDestination to) {
        super.reportFleetJumped(fleet, from, to);

        if (fleet.isPlayerFleet()) {
            if (to != null && to.getDestination() != null && market.getContainingLocation() == to.getDestination().getContainingLocation()) playDansen(0);
            else setVolumeZero();
        }
    }

    private float getAdjustedAmt(float amount) {
        boolean fast = Global.getSector().getCampaignUI().isFastForward();
        return fast ? amount / 2f : amount;
    }

}
