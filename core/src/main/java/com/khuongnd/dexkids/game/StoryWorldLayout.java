package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.story.VisualCue.Prop;
import com.khuongnd.dexkids.story.adventure.StoryEpisode.ObjectSlot;
import com.khuongnd.dexkids.story.adventure.StoryEpisode.Audience;
import com.khuongnd.dexkids.story.adventure.StoryWorldCatalog;

/** Floor anchors and perspective lanes. Portable props retain their real relative size. */
public final class StoryWorldLayout {
    public record Box(float x,float y,float width,float height) {}
    public static Box support(ObjectSlot slot,Prop prop) {return support(slot,prop,Audience.BOTH);}
    public static Box support(ObjectSlot slot,Prop prop,Audience audience) {
        var spec=StoryWorldCatalog.object(prop);
        boolean sky=spec!=null && spec.kind()==StoryWorldCatalog.Kind.SKY || prop==Prop.CLOUD||prop==Prop.BIRD||prop==Prop.BUTTERFLY||prop==Prop.KITE;
        float center=switch(slot){case BACK_LEFT,LEFT->155;case BACK_RIGHT,RIGHT->1755;case FOREGROUND->audience==Audience.BOTH?1080:1550;};
        float floor=switch(slot){case BACK_LEFT,BACK_RIGHT->350;case LEFT,RIGHT->StoryStageGeometry.FLOOR;case FOREGROUND->195;};
        float depth=switch(slot){case BACK_LEFT,BACK_RIGHT->.74f;case LEFT,RIGHT->1f;case FOREGROUND->1.08f;};
        var size=StoryStageGeometry.size(prop,StoryStageGeometry.CHILD_HEIGHT);
        // Buildings, trees and large wildlife live further into the scene, never as toy-sized icons.
        if(size.width()>500||size.height()>500){depth=Math.min(.45f,350/Math.max(size.width(),size.height()));floor=380;center=slot==ObjectSlot.BACK_LEFT||slot==ObjectSlot.LEFT?190:1720;}
        if(sky){floor=slot==ObjectSlot.LEFT||slot==ObjectSlot.BACK_LEFT?580:800;depth=.55f;if(slot==ObjectSlot.FOREGROUND)center=1300;}
        float width=size.width()*depth,height=size.height()*depth;
        if(prop==Prop.BLANKET)height*=.22f; // A cloth laid on the floor, projected into the ground plane.
        if(center-width/2<12)center=12+width/2;if(center+width/2>1908)center=1908-width/2;
        return new Box(center-width/2,floor,width,height);
    }
    private StoryWorldLayout(){}
}
