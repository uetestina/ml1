package at.droelf.clippy.storage;

import android.content.Context;
import android.content.SharedPreferences;

import at.droelf.clippy.model.AgentType;

public class AgentStorage {

    private final static String NAME = "agent_storage";

    private final static String AGENT_MUTE = "agent_mute";
    private final static boolean AGENT_MUTE_DEFAULT = false;

    private final static String AGENT_STOP = "agent_stop";
    private final static boolean AGENT_STOP_DEFAULT = false;

    private final static String AGENT_LAST_USED = "agent_lastused";
    private final static AgentType AGENT_LAST_USED_DEFAUlT = AgentType.CLIPPY;

    private final SharedPreferences sharedPreferences;

    public AgentStorage(Context context){
        this.sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public boolean isMute(){
        return sharedPreferences.getBoolean(AGENT_MUTE, AGENT_MUTE_DEFAULT);
    }

    public void setMute(boolean mute){
        sharedPreferences.edit()
                .putBoolean(AGENT_MUTE, mute)
                .apply();
    }

    public AgentType getLastUsedAgent(){
        final String agentString = sharedPreferences.getString(AGENT_LAST_USED, AGENT_LAST_USED_DEFAUlT.name());
        return AgentType.valueOf(agentString);
    }

    public void setAgentLastUsed(AgentType agentLastUsed){
        sharedPreferences.edit()
                .putString(AGENT_LAST_USED, agentLastUsed.name())
                .apply();
    }

    public boolean isAgentStop(){
        return sharedPreferences.getBoolean(AGENT_STOP, AGENT_STOP_DEFAULT);
    }

    public void setAgentStop(boolean stop){
        sharedPreferences.edit()
                .putBoolean(AGENT_STOP, stop)
                .apply();
    }

}
