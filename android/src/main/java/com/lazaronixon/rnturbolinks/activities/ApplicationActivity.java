package com.lazaronixon.rnturbolinks.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.react.ReactActivity;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;
import com.facebook.react.views.imagehelper.ImageSource;
import com.lazaronixon.rnturbolinks.R;
import com.lazaronixon.rnturbolinks.util.NavBarStyle;
import com.lazaronixon.rnturbolinks.util.TurbolinksAction;
import com.lazaronixon.rnturbolinks.util.TurbolinksRoute;
import com.lazaronixon.rnturbolinks.util.TurbolinksToolbar;

import java.util.ArrayList;

import static android.view.MenuItem.SHOW_AS_ACTION_ALWAYS;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.lazaronixon.rnturbolinks.RNTurbolinksModule.INTENT_INITIAL;
import static com.lazaronixon.rnturbolinks.RNTurbolinksModule.INTENT_ROUTE;

public abstract class ApplicationActivity extends ReactActivity {

    protected static final String TURBOLINKS_ACTION_PRESS = "turbolinksActionPress";
    private static final String TURBOLINKS_TITLE_PRESS = "turbolinksTitlePress";

    protected TurbolinksToolbar toolBar;
    protected TurbolinksRoute route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        route = getIntent().getParcelableExtra(INTENT_ROUTE);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setNavBarStyle(NavBarStyle.getDefault());
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode == KeyEvent.KEYCODE_VOLUME_UP ? KeyEvent.KEYCODE_MENU : keyCode, event);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (route.getActions() != null) {
            for (int i = 0; i < route.getActions().size(); i++) {
                TurbolinksAction action = new TurbolinksAction(route.getActions().get(i));

                MenuItem menuItem = menu.add(Menu.NONE, action.getId(), i, action.getTitle());
                menuItem.setShowAsAction(action.getButton() ? SHOW_AS_ACTION_ALWAYS : SHOW_AS_ACTION_NEVER);
                if (action.getIcon() != null) setMenuItemIcon(action.getIcon(), menuItem);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return false;
        } else {
            handleActionPress(item.getItemId());
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        if (isInitial() || (isModal() && !isDismissable())) {
            moveTaskToBack(true);
        } else {
            super.onBackPressed();
        }
    }

    public void renderTitle(String title, String subtitle) {
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setSubtitle(subtitle);
    }

    public void setNavBarStyle(NavBarStyle style) {
        if (style.getBarTintColor() != null) toolBar.setBackgroundColor(style.getBarTintColor());
        if (style.getTitleTextColor() != null) toolBar.setTitleTextColor(style.getTitleTextColor());
        if (style.getSubtitleTextColor() != null) toolBar.setSubtitleTextColor(style.getSubtitleTextColor());
        if (style.getMenuIcon() != null) setToolbarOverFlowIcon(style.getMenuIcon(), toolBar);
    }

    public void setActions(ArrayList<Bundle> actions) { route.setActions(actions); }

    public abstract void renderComponent(TurbolinksRoute route);

    public abstract void reloadVisitable();

    public abstract void reloadSession();

    public abstract void injectJavaScript(String script);

    protected abstract void handleActionPress(int itemId);

    protected RCTDeviceEventEmitter getEventEmitter() {
        return getReactInstanceManager().getCurrentReactContext().getJSModule(RCTDeviceEventEmitter.class);
    }

    protected void setupToolBar() {
        toolBar = findViewById(R.id.toolbar);
        toolBar.setTitle("");
        toolBar.setVisibleDropDown(route.getVisibleDropDown());
        setSupportActionBar(toolBar);

        setHiddenNavBar();
        setDisplayHomeAsUpEnabled(!isInitial());
        setNavBarStyle(NavBarStyle.getDefault());
    }

    protected void handleTitlePress(final String component, final String url, final String path) {
        toolBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WritableMap params = Arguments.createMap();
                params.putString("component", component);
                params.putString("url", url);
                params.putString("path", path);
                ApplicationActivity.this.getEventEmitter().emit(TURBOLINKS_TITLE_PRESS, params);
            }
        });
    }

    protected boolean isInitial() {
        return getIntent().getBooleanExtra(INTENT_INITIAL, true);
    }

    private void setDisplayHomeAsUpEnabled(boolean displayHomeAsUpEnabled) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(displayHomeAsUpEnabled);
    }

    private void setHiddenNavBar() {
        if (route.getHidesNavBar() || route.getModal()) getSupportActionBar().hide();
    }

    private boolean isModal() { return route.getModal(); }

    private boolean isDismissable() { return route.getDismissable(); }

    private void setToolbarOverFlowIcon(Bundle icon, final Toolbar toolBar) {
        bitmapFor(icon, getApplicationContext(), new BaseBitmapDataSubscriber() {
            @Override
            public void onNewResultImpl(@Nullable Bitmap bitmap) {
                toolBar.setOverflowIcon(new BitmapDrawable(getApplicationContext().getResources(), bitmap));
            }

            @Override
            public void onFailureImpl(DataSource dataSource) {
                Log.e(ApplicationActivity.class.getName(), "Invalid bitmap: ", dataSource.getFailureCause());
            }
        });
    }

    private void setMenuItemIcon(Bundle icon, final MenuItem menuItem) {
        bitmapFor(icon, getApplicationContext(), new BaseBitmapDataSubscriber() {
            @Override
            public void onNewResultImpl(@Nullable Bitmap bitmap) {
                menuItem.setIcon(new BitmapDrawable(getApplicationContext().getResources(), bitmap));
            }

            @Override
            public void onFailureImpl(DataSource dataSource) {
                Log.e(ApplicationActivity.class.getName(), "Invalid bitmap: ", dataSource.getFailureCause());
            }
        });
    }

    private void bitmapFor(Bundle image, Context context, BaseBitmapDataSubscriber baseBitmapDataSubscriber) {
        ImageSource source = new ImageSource(context, image.getString("uri"));
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(source.getUri()).build();

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(imageRequest, context);

        dataSource.subscribe(baseBitmapDataSubscriber, UiThreadImmediateExecutorService.getInstance());
    }
}
