package com.andres.multiwork.pc.highstocks;

public interface AnnotationEvent {

    public void onAnnotationClicked(String title, double mouseX, double mouseY);

    public void onMouseEnter(String title, double mouseX, double mouseY);

    public void onMouseOut(String title, double mouseX, double mouseY);

}
