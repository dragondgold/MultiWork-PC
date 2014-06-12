package com.andres.multiwork.pc.utils;

import java.util.HashMap;

public class ScreenManager {

    private HashMap<String, BuildProcedure> buildProcedureMap = new HashMap<String, BuildProcedure>();
    private HashMap<String, MultiWorkScreen> screenMap = new HashMap<String, MultiWorkScreen>();

    public ScreenManager(){}

    public void show(String tag){
        if(screenMap.containsKey(tag)){
            MultiWorkScreen screen = screenMap.get(tag);

            buildProcedureMap.get(tag).show(screen.getStage(), screen, screen.getScene());
        }
    }

    public MultiWorkScreen build(String tag){
        MultiWorkScreen screen = buildProcedureMap.get(tag).build();
        screenMap.put(tag, screen);

        return screen;
    }

    public MultiWorkScreen buildAndShowScreen(String tag){
        MultiWorkScreen multiWorkScreen = build(tag);
        show(tag);

        return multiWorkScreen;
    }

    public void addScreen(String tag, BuildProcedure buildProcedure){
        buildProcedureMap.put(tag, buildProcedure);
    }

    public MultiWorkScreen getScreen(String tag){
        return screenMap.get(tag);
    }

}
