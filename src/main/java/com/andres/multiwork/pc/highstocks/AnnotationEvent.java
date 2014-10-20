package com.andres.multiwork.pc.highstocks;

public interface AnnotationEvent {

    public void onAnnotationClicked(String title, double mouseX, double mouseY);

    public void onEnterAnnotation(String title);

    public void onLeaveAnnotation(String title);

}
