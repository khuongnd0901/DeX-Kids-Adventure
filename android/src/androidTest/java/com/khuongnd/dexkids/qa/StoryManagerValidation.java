package com.khuongnd.dexkids.qa;

import android.app.*;
import android.content.*;
import android.graphics.Bitmap;
import android.os.*;
import android.view.*;
import android.view.accessibility.*;
import android.widget.*;
import com.khuongnd.dexkids.story.StoryRepository;
import com.khuongnd.dexkids.story.VisualCue;
import java.io.*;
import java.util.*;

/** Real manager Views/dialogs, actual F10/result replay, no HTTP. No production method is invoked reflectively. */
public final class StoryManagerValidation extends Instrumentation {
    private Activity child,manager;
    private File evidence;
    @Override public void onCreate(Bundle args){super.onCreate(args);start();}
    @Override public void onStart(){Bundle result=new Bundle();int code=Activity.RESULT_OK;try{run(result);}catch(Throwable failure){code=Activity.RESULT_CANCELED;result.putString("FAIL",failure.getClass().getSimpleName()+": "+failure.getMessage());}finally{finish(code,result);}}
    private void run(Bundle result) throws Exception {
        require(android.os.Build.HARDWARE.equals("ranchu")||android.os.Build.HARDWARE.equals("goldfish"),"Explicit emulator only");
        Context context=getTargetContext();var ai=context.getSharedPreferences("kids_ai_settings_v1",0);var parent=context.getSharedPreferences("parent_settings",0);
        Map<String,?> oldAi=new HashMap<>(ai.getAll()),oldParent=new HashMap<>(parent.getAll());
        StoryRepository repository=new StoryRepository(context);var baseline=repository.get("garden-both");int oldRevision=baseline==null?0:baseline.getEpisode().revision();
        evidence=new File(context.getExternalFilesDir(null),"qa-t057-manager");require(evidence.isDirectory()||evidence.mkdirs(),"Evidence directory");
        var monitor=addMonitor("com.khuongnd.dexkids.StoryManagerActivity",null,false);
        try {
            require(ai.edit().putBoolean("ai_quizzes",false).putBoolean("creative_trip_packs",false).putBoolean("automatic_trip_packs",false).putBoolean("child_text_cloud_explicit",false).commit(),"Offline prefs");
            parent.edit().putString("audience_mode","BOTH").putBoolean("child_mic_optin",false).commit();
            child=startActivitySync(new Intent().setClassName(context,"com.khuongnd.dexkids.KidsActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            SystemClock.sleep(4500);sendKeyDownUpSync(KeyEvent.KEYCODE_F10);click("Thư viện truyện");
            manager=waitForMonitorWithTimeout(monitor,10000);require(manager!=null,"F10 did not launch manager");waitWorker();
            click("Thêm truyện mẫu offline đã biên soạn");click("Cả hai · học theo tuổi 4");waitWorker();
            var seed=repository.get("garden-both");require(seed!=null && seed.getEpisode().revision()>oldRevision && !seed.getApproved(),"UI seed must create unapproved fixture");shot("01-library-draft");
            click("Tìm truyện");setVisibleEdit("Khu vườn");click("Tìm");waitWorker();
            require("Khu vườn".equals(field(manager,"query")),"Search input not applied");
            click("Lọc người xem");click("Cả hai");waitWorker();require("BOTH".equals(field(manager,"audienceFilter").toString()),"Audience filter not applied");
            openEntry(repository,"garden-both");shot("02-preview");click("Đóng");
            openEntry(repository,"garden-both");click("Thao tác");click("Duyệt để sử dụng");shot("03-parent-approval-confirmation");click("Duyệt");waitWorker();require(repository.get("garden-both").getApproved(),"UI approval missing");shot("04-library-approved");
            openEntry(repository,"garden-both");click("Thao tác");click("Sửa lời kể / câu hỏi");shot("05-editor");click("Hủy");require(repository.get("garden-both").getEpisode().revision()==seed.getEpisode().revision(),"Cancelled editor changed DB");
            openEntry(repository,"garden-both");click("Thao tác");click("Sửa lời kể / câu hỏi");
            setVisibleEdit("Trong câu chuyện tưởng tượng, Capybara ngước nhìn bạn chim trên cành cây.");click("Lưu và duyệt lại AI");waitWorker();
            var edited=repository.get("garden-both");require(!edited.getApproved() && !edited.getReviewed(),"Offline edit retained approval");
            var beat=edited.getEpisode().scenes().get(0).beats().get(0);require(beat.action()==VisualCue.Action.LOOK_UP && beat.prop()==VisualCue.Prop.BIRD,"Edited visual not rebuilt");shot("06-offline-edited-draft");
            openEntry(repository,"garden-both");click("Thao tác");click("Xóa truyện");click("Hủy");require(!repository.get("garden-both").getDeleted(),"Cancelled delete changed DB");
            openEntry(repository,"garden-both");click("Thao tác");click("Hoàn tác phiên bản");waitWorker();require(repository.get("garden-both").getApproved(),"UI undo did not restore approved revision");
            openEntry(repository,"garden-both");click("Thao tác");click("Phát lại offline");SystemClock.sleep(3000);require(manager.isFinishing()||manager.isDestroyed(),"Manager did not return playback result");
            require("garden-both".equals(child.getIntent().getStringExtra("story_episode_id")),"F10 playback result not delivered");shot("07-replay-result");
            result.putString("t057_manager","PASS: actual F10 launch; seed; search; audience filter; scroll preview; parent approval; edit cancel; offline edit cue repair and approval invalidation; delete cancel; undo; replay Activity result; AI disabled");
            result.putString("screenshots",evidence.getAbsolutePath());
        } finally {
            runOnMainSync(()->{if(manager!=null&&!manager.isFinishing())manager.finish();if(child!=null)child.finish();});
            var current=repository.get("garden-both");
            if(oldRevision==0 && current!=null){repository.delete("garden-both",true);repository.purgeDeleted("garden-both");}
            else while(current!=null && current.getEpisode().revision()>oldRevision){repository.undo("garden-both");current=repository.get("garden-both");}
            repository.close();restore(ai,oldAi);restore(parent,oldParent);removeMonitor(monitor);
        }
    }
    private void openEntry(StoryRepository repo,String id) throws Exception {
        String query=(String)field(manager,"query");Object audience=field(manager,"audienceFilter");
        var entries=repo.list(false).stream().filter(e->(audience==null||e.getEpisode().audience().toString().equals(audience.toString()))&&(e.getEpisode().title()+" "+e.getEpisode().educationalGoal()).toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))).collect(java.util.stream.Collectors.toList());
        int index=-1;for(int i=0;i<entries.size();i++)if(entries.get(i).getEpisode().id().equals(id))index=i;
        require(index>=0,"Target row missing");final int position=index+10;
        runOnMainSync(()->{try{LinearLayout column=(LinearLayout)field(manager,"column");View row=column.getChildAt(position);require(row instanceof Button && row.performClick(),"Actual story row click failed");}catch(Exception e){throw new AssertionError(e);}});SystemClock.sleep(350);
    }
    private void waitWorker() throws Exception {long until=SystemClock.elapsedRealtime()+15000;do{waitForIdleSync();if(manager!=null && Boolean.FALSE.equals(field(manager,"busy"))){SystemClock.sleep(350);waitForIdleSync();return;}SystemClock.sleep(100);}while(SystemClock.elapsedRealtime()<until);throw new AssertionError("Manager worker timed out");}
    private void click(String text){long until=SystemClock.elapsedRealtime()+7000;while(SystemClock.elapsedRealtime()<until){AccessibilityNodeInfo root=getUiAutomation().getRootInActiveWindow();if(root!=null)for(var node:root.findAccessibilityNodeInfosByText(text)){if(node.getText()!=null&&node.getText().toString().equals(text)){AccessibilityNodeInfo target=node;while(target!=null&&!target.isClickable())target=target.getParent();if(target!=null&&target.performAction(AccessibilityNodeInfo.ACTION_CLICK)){SystemClock.sleep(350);return;}}}SystemClock.sleep(100);}throw new AssertionError("Visible UI action missing: "+text);}
    private void setVisibleEdit(String text){AccessibilityNodeInfo root=getUiAutomation().getRootInActiveWindow();AccessibilityNodeInfo edit=findEdit(root);require(edit!=null,"Visible editor missing");Bundle args=new Bundle();args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,text);require(edit.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args),"Set UI editor text failed");}
    private AccessibilityNodeInfo findEdit(AccessibilityNodeInfo node){if(node==null)return null;if("android.widget.EditText".contentEquals(node.getClassName())&&node.isVisibleToUser())return node;for(int i=0;i<node.getChildCount();i++){var found=findEdit(node.getChild(i));if(found!=null)return found;}return null;}
    private void shot(String name) throws Exception {SystemClock.sleep(300);Bitmap bitmap=getUiAutomation().takeScreenshot();require(bitmap!=null&&bitmap.getWidth()==1920&&bitmap.getHeight()==1080,"FullHD screenshot required");try(var output=new FileOutputStream(new File(evidence,name+".png"))){require(bitmap.compress(Bitmap.CompressFormat.PNG,100,output),"Screenshot encoding");}finally{bitmap.recycle();}}
    private static Object field(Object object,String name)throws Exception{var field=object.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(object);}
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    private static void restore(android.content.SharedPreferences prefs,Map<String,?> rows){var editor=prefs.edit().clear();for(var row:rows.entrySet()){Object value=row.getValue();if(value instanceof String)editor.putString(row.getKey(),(String)value);else if(value instanceof Boolean)editor.putBoolean(row.getKey(),(Boolean)value);else if(value instanceof Integer)editor.putInt(row.getKey(),(Integer)value);else if(value instanceof Long)editor.putLong(row.getKey(),(Long)value);else if(value instanceof Float)editor.putFloat(row.getKey(),(Float)value);}require(editor.commit(),"Preference restoration");}
}
