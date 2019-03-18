/*
 * PallyCon Team ( http://www.pallycon.com )
 *
 * This is a simple example project to show how to build a APP using the PallyCon Widevine SDK
 * The SDK is based on Exo player library
 */

package com.pallycon.exoplayersample;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.KeysExpiredException;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.ui.TrackSelectionView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.pallycon.widevinelibrary.DatabaseDecryptException;
import com.pallycon.widevinelibrary.DetectedDeviceTimeModifiedException;
import com.pallycon.widevinelibrary.NetworkConnectedException;
import com.pallycon.widevinelibrary.PallyconDrmException;
import com.pallycon.widevinelibrary.PallyconEncrypterException;
import com.pallycon.widevinelibrary.PallyconEventListener;
import com.pallycon.widevinelibrary.PallyconServerResponseException;
import com.pallycon.widevinelibrary.PallyconWVMSDK;
import com.pallycon.widevinelibrary.PallyconWVMSDKFactory;
import com.pallycon.widevinelibrary.UnAuthorizedDeviceException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Created by PallyconTeam
 */

public class PlayerActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "pallycon_sampleapp";
    public static final String CONTENTS_TITLE = "contents_title";
    public static final String DRM_SCHEME_UUID_EXTRA = "drm_scheme_uuid";
    public static final String DRM_LICENSE_URL = "drm_license_url";
    public static final String DRM_USERID = "drm_userid";
    public static final String DRM_OID = "drm_oid";
    public static final String DRM_CID = "drm_cid";
    public static final String DRM_TOKEN = "drm_token";
    public static final String DRM_CUSTOM_DATA = "drm_custom_data";
    public static final String DRM_MULTI_SESSION = "drm_multi_session";
    public static final String DRM_COOKIE = "drm_cookie";
    public static final String THUMB_URL = "thumb_url";
    public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";

    private DataSource.Factory mediaDataSourceFactory;
    private DefaultBandwidthMeter bandwidthMeter;
    private SimpleExoPlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    private boolean shouldAutoPlay;
    private Handler eventHandler;
    private PallyconWVMSDK WVMAgent;
    private LinearLayout debugRootView;

    private DefaultTrackSelector trackSelector;
    private DefaultTrackSelector.Parameters trackSelectorParameters;
    private String cookie;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    // TODO : must implement ExoPlayer.EventListener
    private Player.EventListener playerEventListener = new Player.DefaultEventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            //TODO: Please refer to the ExoPlayer guide.
            Log.d(TAG, "onTimelineChanged()!!");
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            //TODO: Please refer to the ExoPlayer guide.
            Log.d(TAG, "onTracksChanged()!!");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            //TODO: Please refer to the ExoPlayer guide.
            Log.d(TAG, "onLoadingChanged(" + isLoading + ")");
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            //TODO: Please refer to the ExoPlayer guide.
            updateButtonVisibilities();

//            //// Chromecast
//            switch( playbackState ) {
//                case ExoPlayer.STATE_IDLE:
//                    mPlayStatus.mCurrentState = PlayStatus.STATE_IDLE;
//                    break;
//                case ExoPlayer.STATE_BUFFERING:
//                    mPlayStatus.mCurrentState = PlayStatus.STATE_BUFFERING;
//                    break;
//                case ExoPlayer.STATE_READY:
//                    if( playWhenReady ) {
//                        mPlayStatus.mCurrentState = PlayStatus.STATE_PLAYING;
//                    }
//                    else {
//                        mPlayStatus.mCurrentState = PlayStatus.STATE_PAUSED;
//                    }
//
//                    break;
//                case ExoPlayer.STATE_ENDED:
//                    mPlayStatus.mCurrentState = PlayStatus.STATE_IDLE;
//                    break;
//                default:
//                    mPlayStatus.mCurrentState = PlayStatus.STATE_IDLE;
//            }
//            //// CC
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            // TODO: Check the types of errors that occur inside the player.
            String errorString;
            if (e.type == ExoPlaybackException.TYPE_RENDERER) {
                Exception cause = e.getRendererException();
                errorString = cause.toString();

            } else if (e.type == ExoPlaybackException.TYPE_SOURCE) {
                Exception cause = e.getSourceException();
                errorString = cause.toString();

            } else if (e.type == ExoPlaybackException.TYPE_UNEXPECTED) {
                Exception cause = e.getUnexpectedException();
                errorString = cause.toString();
            } else {
                errorString = e.toString();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
            builder.setTitle("Play Error");
            builder.setMessage(errorString);
            builder.setPositiveButton("OK", null);
            Dialog dialog = builder.create();
            dialog.show();
        }

        @Override
        public void onPositionDiscontinuity(int reason) {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onSeekProcessed() {

        }
    };

    // TODO : must implement PallyconEventListener
    private PallyconEventListener pallyconEventListener = new PallyconEventListener() {
        @Override
        public void onDrmKeysLoaded(Map<String, String> licenseInfo) {
            // TODO: Use the loaded license information.
            StringBuilder stringBuilder = new StringBuilder();

            Iterator<String> keys = licenseInfo.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = licenseInfo.get(key);
                try {
                    if (Long.parseLong(value) == 0x7fffffffffffffffL) {
                        value = "Unlimited";
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                }
                stringBuilder.append(key).append(" : ").append(value);
                if (keys.hasNext()) {
                    stringBuilder.append("\n");
                }
            }

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PlayerActivity.this);
            alertBuilder.setTitle("License Info");
            alertBuilder.setMessage(stringBuilder.toString());
            alertBuilder.setPositiveButton("OK", null);
            Dialog dialog = alertBuilder.create();
            dialog.show();
        }

        @Override
        public void onDrmSessionManagerError(Exception e) {
            // TODO: Handle exceptions in error situations. Please refer to the API guide document for details of exception.
            AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
            builder.setTitle("DrmManager Error");

            if (e instanceof NetworkConnectedException) {
                builder.setMessage(e.getMessage());

            } else if (e instanceof PallyconServerResponseException) {
                PallyconServerResponseException e1 = (PallyconServerResponseException) e;
                builder.setMessage("errorCode : " + e1.getErrorCode() + "\n" + "message : " + e1.getMessage());

            } else if (e instanceof KeysExpiredException) {
                builder.setMessage("license has been expired. please remove the license first and try again.");
                builder.setPositiveButton("OK", null);
                Dialog dialog = builder.create();
                dialog.show();
                return;

            } else if(e instanceof DatabaseDecryptException) {
                builder.setMessage("errorMsg : " + e.getMessage());
                builder.setPositiveButton("OK", null);
                Dialog dialog = builder.create();
                dialog.show();
                return;

            } else if (e instanceof DetectedDeviceTimeModifiedException) {
                // TODO: content playback should be prohibited to prevent illegal use of content.
                builder.setMessage("Device time has been changed. go to [Settings] > [Date & time] and use [Automatic date & time] and Connect Internet");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                Dialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                return;

            } else {
                builder.setMessage(e.getMessage());
            }

            builder.setPositiveButton("OK", null);
            Dialog dialog = builder.create();
            dialog.show();
        }

        @Override
        public void onDrmKeysRestored() {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PlayerActivity.this);
            alertBuilder.setTitle("License Info");
            alertBuilder.setMessage("Drm key Restored !!!!!");
            alertBuilder.setPositiveButton("OK", null);
            Dialog dialog = alertBuilder.create();
            dialog.show();
        }

        @Override
        public void onDrmKeysRemoved() {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PlayerActivity.this);
            alertBuilder.setTitle("License Info");
            alertBuilder.setMessage("Drm key Removed !!!!!");
            alertBuilder.setPositiveButton("OK", null);
            Dialog dialog = alertBuilder.create();
            dialog.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        debugRootView = findViewById(R.id.controls_root);

        Log.d(TAG, "onCreate");

        shouldAutoPlay = true;
        simpleExoPlayerView = findViewById(R.id.player_view);
        simpleExoPlayerView.requestFocus();

//		//// Chromecast
//		mCastContext = CastContext.getSharedInstance(this);
//		// mCastContext is always not null. No need to check null
//		createMessageReceivedCallback();
//		createSessionManagerListener();
//		createRemoteMediaClientListener();
//		createRemoteMediaClientProgressListener();
//		mSessionManager = mCastContext.getSessionManager();
//		mCastSession = mSessionManager.getCurrentCastSession();
//		if( mCastSession != null ) {
//			Log.d(TAG, "[CAST] CastSession exists");
//			try {
//				mCastSession.setMessageReceivedCallbacks(CAST_MSG_NAMESPACE, mMessageReceivedCallback);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		} else {
//			Log.d(TAG, "mCastSession is null");
//		}
//
//		bNowOnChromecast = false;
//		bCreated = true;
//		//// CC

        eventHandler = new Handler();
        bandwidthMeter = new DefaultBandwidthMeter();

        Intent intent = getIntent();
        cookie = intent.getStringExtra(DRM_COOKIE);
        mediaDataSourceFactory = buildDataSourceFactory();

        trackSelectorParameters = new DefaultTrackSelector.ParametersBuilder().build();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

//		// Check chromecast first
//		//// Chromecast
//		mSessionManager.addSessionManagerListener(mSessionManagerListener, CastSession.class);
//		if (mCastSession != null && (mCastSession.isConnected() || mCastSession.isConnecting())) {
//			if( bCreated ) {
//				// If PlayerActivity is created, it means that the user select a content at MainActivity.
//				releaseCast();
//				bCreated = false;
//				bNowOnChromecast = false;
//			}
//
//			if( bNowOnChromecast ) {
//				// If the content is playing at Chromecast already, do nothing.
//				Log.d(TAG, "[CAST] continue playing on Chromecast");
//				return;
//			}
//
//			// If the content is not playing at Chromecast, load the content.
//			Uri contentUri = getIntent().getData();
//			if( contentUri != null && contentUri.toString().startsWith("/") ) {
//				// The contents is in internal storage. It is not supported by Cast.
//				mPlayStatus.mScreen = PlayStatus.SCREEN_LOCAL;
//				Log.d(TAG, "[CAST] Chromecast is connected but the content type is not streaming");
//			} else {
//				// The contents is in remote storage. Cast will be work.
//				mPlayStatus.mScreen = PlayStatus.SCREEN_CAST;
//				Log.d(TAG, "[CAST] Chromecast is connected");
//
//				Log.d(TAG, "[CAST] start playing on Chromecast");
//				if( !loadRemoteMedia(0, true) )
//					Log.d(TAG, "[CAST] failure on loadRemoteMedia()");
//
//				return;
//			}
//		} else {
//			mPlayStatus.mScreen = PlayStatus.SCREEN_LOCAL;
//			Log.d(TAG, "Chromecast is not connected");
//		}
//
//		long position = mPlayStatus.mPosition;
//		releaseCast();
//		mPlayStatus.mPosition = position;
//		//// CC
//
//		// If the CastControllerActivity is destroyed when it is full screen activity, onSessionEnded() of Chromecast will not be called.
//		// PlayActivity will come to the foreground, and onResume() will be called.
//		if( mPlayStatus.mCurrentState == PlayStatus.STATE_PLAYING )
//			shouldAutoPlay = true;

        if (Util.SDK_INT <= 23 || player == null) {
            try {
                initializePlayer();
            } catch (PallyconDrmException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            } catch (PallyconEncrypterException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            } catch (JSONException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            simpleExoPlayerView.setUseController(true);
            player.setPlayWhenReady(shouldAutoPlay);
        }
    }

    @Override
    protected void onPause() {
//		//// Chromecast
//		if( mCastContext != null && mSessionManagerListener != null && mRemoteClient != null ) {
//			mSessionManager.removeSessionManagerListener(mSessionManagerListener, CastSession.class);
//		}
//		//// CC

        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }

        super.onPause();
    }

    @Override
    protected void onStop() {
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
//		//// Chromecast
//		mPlayStatus.clear();
//		//// CC

        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                initializePlayer();
            } catch (PallyconDrmException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            } catch (PallyconEncrypterException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            } catch (JSONException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.storage_permission_denied, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializePlayer() throws PallyconDrmException, PallyconEncrypterException, JSONException {
        UUID drmSchemeUuid = null;
        Intent intent = getIntent();
        Uri uri = intent.getData();

        if (uri == null || uri.toString().length() < 1)
            throw new PallyconDrmException("The content url is missing");

        if (player == null) {
            TrackSelection.Factory adaptiveTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);
            trackSelector.setParameters(trackSelectorParameters);

            if (intent.hasExtra(DRM_SCHEME_UUID_EXTRA)) {
                drmSchemeUuid = UUID.fromString(intent.getStringExtra(DRM_SCHEME_UUID_EXTRA));
            }

            DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
            if (drmSchemeUuid != null) {
                String drmLicenseUrl = intent.getStringExtra(DRM_LICENSE_URL);
                boolean multiSession = intent.getBooleanExtra(DRM_MULTI_SESSION, false);
                try {
                    // TODO : Acquire Pallycon Widevine module.
                    WVMAgent = PallyconWVMSDKFactory.getInstance(this);
                    WVMAgent.setPallyconEventListener(pallyconEventListener);
                } catch (PallyconDrmException e) {
                    e.printStackTrace();
                } catch (UnAuthorizedDeviceException e) {
                    e.printStackTrace();
                }

                try {
                    String userId = intent.getStringExtra(DRM_USERID);
                    String cid = intent.getStringExtra(DRM_CID);
                    String oid = intent.getStringExtra(DRM_OID);
                    String token = intent.getStringExtra(DRM_TOKEN);
                    String customData = intent.getStringExtra(DRM_CUSTOM_DATA);
                    // TODO : Create Pallycon drmSessionManager to get into ExoPlayerFactory

                    if (token.equals("") == false) {
                        drmSessionManager = WVMAgent.createDrmSessionManagerByToken(drmSchemeUuid, drmLicenseUrl, uri, userId, cid, token, multiSession);
                    } else if (customData.equals("") == false) {
                        drmSessionManager = WVMAgent.createDrmSessionManagerByCustomData(drmSchemeUuid, drmLicenseUrl, uri, customData, multiSession);
                    } else if (userId == null || userId.length() < 1) {
                        drmSessionManager = WVMAgent.createDrmSessionManagerByProxy(drmSchemeUuid, drmLicenseUrl, uri, cid, multiSession);
                    } else {
                        drmSessionManager = WVMAgent.createDrmSessionManager(drmSchemeUuid, drmLicenseUrl, uri, userId, cid, oid, multiSession);
                    }

                } catch (PallyconDrmException e) {
                    e.printStackTrace();
                    throw e;
                }
            }

            boolean preferExtensionDecoders = intent.getBooleanExtra(PREFER_EXTENSION_DECODERS, false);
            @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode = DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON;

            // TODO : Set Pallycon drmSessionManager for drm controller.
            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this,
                    drmSessionManager, extensionRendererMode);

            player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
            // TODO : Set Pallycon drmSessionManager for listener.
            player.addListener(playerEventListener);

            // TODO : Set Sercurity API to protect media recording by screen recorder
            SurfaceView view = (SurfaceView) simpleExoPlayerView.getVideoSurfaceView();
            if (Build.VERSION.SDK_INT >= 17) {
                view.setSecure(true);
            }

            simpleExoPlayerView.setPlayer(player);

//			//// Chromecast
//			if( mPlayStatus.mPosition >  0 )
//				player.seekTo(mPlayStatus.mPosition);
//			//// CC
            player.setPlayWhenReady(shouldAutoPlay);
        }

        if (Util.maybeRequestReadExternalStoragePermission(this, uri)) {
            return;
        }

        MediaSource mediaSource = buildMediaSource(uri);
        player.prepare(mediaSource);
        updateButtonVisibilities();
    }

    private void releasePlayer() {
        if (player != null) {
            shouldAutoPlay = player.getPlayWhenReady();
            player.release();
            player = null;
            trackSelector = null;
        }
    }

    private void updateButtonVisibilities() {
        debugRootView.removeAllViews();

        if (player == null) {
            return;
        }

        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo == null) {
            return;
        }

        if (mappedTrackInfo == null) {
            return;
        }

        for (int i = 0; i < mappedTrackInfo.length; i++) {
            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
            if (trackGroups.length != 0) {
                Button button = new Button(this);
                String label;
                switch (player.getRendererType(i)) {
                    case C.TRACK_TYPE_AUDIO:
                        label = "Audio";
                        break;
                    case C.TRACK_TYPE_VIDEO:
                        label = "Video";
                        break;
                    case C.TRACK_TYPE_TEXT:
                        label = "Text";
                        break;
                    default:
                        continue;
                }
                button.setText(label);
                button.setTag(i);
                button.setOnClickListener(this);
                debugRootView.addView(button, debugRootView.getChildCount() - 1);
            }
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        int type = Util.inferContentType(uri.getLastPathSegment());
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(), new DefaultDashChunkSource.Factory(mediaDataSourceFactory), eventHandler, null);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(), eventHandler, null);
            case C.TYPE_HLS:
            case C.TYPE_SS:
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    private DataSource.Factory buildDataSourceFactory() {
        HttpDataSource.Factory httpDataSourceFactory = buildHttpDataSourceFactory();
        httpDataSourceFactory.setDefaultRequestProperty("Cookie", cookie);
        return new DefaultDataSourceFactory(this, null, httpDataSourceFactory);
    }

    private HttpDataSource.Factory buildHttpDataSourceFactory() {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "ExoPlayerSample"), null);
    }

    @Override
    public void onClick(View view) {
        if (view.getParent() == debugRootView) {
            MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
            if (mappedTrackInfo != null) {
                CharSequence title = ((Button) view).getText();
                int rendererIndex = (int) view.getTag();
                int rendererType = mappedTrackInfo.getRendererType(rendererIndex);
                boolean allowAdaptiveSelections =
                        rendererType == C.TRACK_TYPE_VIDEO
                                || (rendererType == C.TRACK_TYPE_AUDIO
                                && mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                                == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_NO_TRACKS);
                Pair<android.app.AlertDialog, TrackSelectionView> dialogPair =
                        TrackSelectionView.getDialog(this, title, trackSelector, rendererIndex);
                dialogPair.second.setShowDisableOption(true);
                dialogPair.second.setAllowAdaptiveSelections(allowAdaptiveSelections);
                dialogPair.first.show();
            }
        }
    }

//	//// Chromecast
//	private CastContext mCastContext = null;
//	private CastSession mCastSession = null;
//	private SessionManager mSessionManager = null;
//	private SessionManagerListener<CastSession> mSessionManagerListener = null;
//	private RemoteMediaClient mRemoteClient = null;
//	private RemoteMediaClient.Listener			mRemoteClientListener = null;
//	private RemoteMediaClient.ProgressListener	mRemoteClientProgressListener = null;
//	private Cast.MessageReceivedCallback		mMessageReceivedCallback = null;
//	private PlayStatus mPlayStatus = PlayStatus.getObject();
//	private boolean bNowOnChromecast = false;
//	private boolean bCreated = false;
//	private static boolean bCastReceiverRegistered = false;
//	private final String CAST_MSG_NAMESPACE = "urn:x-cast:com.pallycon.cast";
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		super.onCreateOptionsMenu(menu);
//		getMenuInflater().inflate(R.menu.browse, menu);
//		CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);
//		return true;
//	}
//
//	private void releaseCast() {
//		Log.d(TAG, "[CAST] releaseCast()");
//
//		bNowOnChromecast = false;
//		mPlayStatus.clear();
//
//		if( mRemoteClient != null ) {
//			Log.d(TAG, "[CAST] remote client is not null. releasing...");
//			if (mRemoteClient.getCurrentItem() != null) {
//				Log.d(TAG, "[CAST] there is a loaded content. stopping it...");
//				mRemoteClient.stop();
//			}
//
//			removeCastReceivers();
//			mRemoteClient = null;
//		} else {
//			bCastReceiverRegistered = false;
//		}
//	}
//
//	private void addCastReceivers() {
//		if( !bCastReceiverRegistered ) {
//			mRemoteClient.addListener(mRemoteClientListener);
//			mRemoteClient.addProgressListener(mRemoteClientProgressListener, 0);
//			bCastReceiverRegistered = true;
//		}
//	}
//	private void removeCastReceivers() {
//		//if( bCastReceiverRegistered ) {
//			mRemoteClient.removeListener(mRemoteClientListener);
//			mRemoteClient.removeProgressListener(mRemoteClientProgressListener);
//			bCastReceiverRegistered = false;
//		//}
//	}
//
//	private void createSessionManagerListener() {
//		mSessionManagerListener = new SessionManagerListener<CastSession>() {
//			@Override
//			public void onSessionEnded(CastSession session, int error) {
//				Log.d(TAG, "[CAST] onSessionEnded()");
//				onApplicationDisconnected();
//			}
//
//			@Override
//			public void onSessionResumed(CastSession session, boolean wasSuspended) {
//				Log.d(TAG, "[CAST] onSessionResumed()");
//				onApplicationConnected(session);
//			}
//
//			@Override
//			public void onSessionResumeFailed(CastSession session, int error) {
//				Log.d(TAG, "[CAST] onSessionResumeFailed()");
//				onApplicationDisconnected();
//			}
//
//			@Override
//			public void onSessionStarted(CastSession session, String sessionId) {
//				Log.d(TAG, "[CAST] onSessionStarted()");
//				onApplicationConnected(session);
//			}
//
//			@Override
//			public void onSessionStartFailed(CastSession session, int error) {
//				Log.d(TAG, "[CAST] onSessionStartFailed()");
//				onApplicationDisconnected();
//			}
//
//			@Override
//			public void onSessionStarting(CastSession session) {
//				Log.d(TAG, "[CAST] onSessionStarting()");
//			}
//
//			@Override
//			public void onSessionEnding(CastSession session) {
//				Log.d(TAG, "[CAST] onSessionEnding()");
//			}
//
//			@Override
//			public void onSessionResuming(CastSession session, String sessionId) {
//				Log.d(TAG, "[CAST] onSessionResuming()");
//			}
//
//			@Override
//			public void onSessionSuspended(CastSession session, int reason) {
//				Log.d(TAG, "[CAST] onSessionSuspended()");
//			}
//
//			private void onApplicationConnected(CastSession castSession) {
//				mCastSession = castSession;
//				getRemoteMediaClient();
//
//				try {
//					mCastSession.setMessageReceivedCallbacks(CAST_MSG_NAMESPACE, mMessageReceivedCallback);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//
//				mPlayStatus.mPosition = 0;
//				boolean playWhenReady = true;
//
//				mPlayStatus.mScreen = PlayStatus.SCREEN_CAST;
//				if( player != null ) {
//					mPlayStatus.mPosition = player.getCurrentPosition();
//					playWhenReady = player.getPlayWhenReady();
//
//					// pause local player
//					player.setPlayWhenReady(false);
//
//					// disable ui of local player
//					simpleExoPlayerView.setUseController(false);
//				}
//
//				loadRemoteMedia(mPlayStatus.mPosition, playWhenReady);
//			}
//
//			private void onApplicationDisconnected() {
//				// backup latest position before release
//				long latestPosition = mPlayStatus.mPosition;
//				releaseCast();
//
//				mPlayStatus.mScreen = PlayStatus.SCREEN_LOCAL;
//
//				// enable ui of local player
//				simpleExoPlayerView.setUseController(true);
//
//				if( player == null ) {
//					Log.d(TAG, "[CAST] no local player. initializing...");
//					try {
//						initializePlayer();
//					} catch (PallyconDrmException e) {
//						e.printStackTrace();
//					} catch (PallyconEncrypterException e) {
//						e.printStackTrace();
//					} catch (JSONException e) {
//						e.printStackTrace();
//					}
//				}
//
//				// seek position of local player to position of remote player
//				if( player != null ) {
//					player.seekTo(latestPosition);
//					player.setPlayWhenReady(shouldAutoPlay);
//				}
//			}
//		};
//	}
//
//	private String getIdleReasonString(int idleReason) {
//		switch(idleReason) {
//		case MediaStatus.IDLE_REASON_FINISHED:	// 1
//			return "Finish";
//		case MediaStatus.IDLE_REASON_CANCELED:	// 2
//			return "Canceled";
//		case MediaStatus.IDLE_REASON_INTERRUPTED:	// 3
//			return "Interrupted";
//		case MediaStatus.IDLE_REASON_ERROR:	// 4
//			return "Error";
//		case MediaStatus.IDLE_REASON_NONE:	// 0
//		default:
//			return "None";
//		}
//	}
//
//	private void createRemoteMediaClientListener() {
//		mRemoteClientListener = new RemoteMediaClient.Listener() {
//			@Override
//			public void onStatusUpdated() {
//				Log.d(TAG, "[CAST] onStatusUpdated()");
//
//				if( mRemoteClient == null )
//					return;
//
//				switch( mRemoteClient.getPlayerState() ) {
//					case MediaStatus.PLAYER_STATE_IDLE:
//						int idleReason = mRemoteClient.getIdleReason();
//						Log.d(TAG, "[CAST] RemoteMediaClient.getPlayerState(): PLAYER_STATE_IDLE (" + getIdleReasonString(idleReason) +")");
//						mPlayStatus.mCurrentState = PlayStatus.STATE_IDLE;
//
//						if( idleReason == MediaStatus.IDLE_REASON_FINISHED ) {
//							// starting cast after finish, idel reason is 'FINISH' yet.
//							// app must handel 'FINISH' at start
//							if( mPlayStatus.mPosition > 0 ) {
//								releaseCast();
//								finish();
//							}
//						}
//						break;
//					case MediaStatus.PLAYER_STATE_BUFFERING:
//						Log.d(TAG, "[CAST] RemoteMediaClient.getPlayerState(): PLAYER_STATE_BUFFERING");
//						mPlayStatus.mCurrentState = PlayStatus.STATE_BUFFERING;
//						break;
//					case MediaStatus.PLAYER_STATE_PLAYING:
//						Log.d(TAG, "[CAST] RemoteMediaClient.getPlayerState(): PLAYER_STATE_PLAYING");
//						mPlayStatus.mCurrentState = PlayStatus.STATE_PLAYING;
//						shouldAutoPlay = true;
//						break;
//					case MediaStatus.PLAYER_STATE_PAUSED:
//						Log.d(TAG, "[CAST] RemoteMediaClient.getPlayerState(): PLAYER_STATE_PAUSED");
//						mPlayStatus.mCurrentState = PlayStatus.STATE_PAUSED;
//						shouldAutoPlay = false;
//						break;
//					case MediaStatus.PLAYER_STATE_UNKNOWN:
//					default:
//						Log.d(TAG, "[CAST] RemoteMediaClient.getPlayerState(): PLAYER_STATE_UNKNOWN");
//						break;
//				}
//			}
//
//			@Override
//			public void onMetadataUpdated() {
////				Log.d(TAG, "[CAST] onMetadataUpdated()");
//			}
//
//			@Override
//			public void onQueueStatusUpdated() {
////				Log.d(TAG, "[CAST] onQueueStatusUpdated()");
//			}
//
//			@Override
//			public void onPreloadStatusUpdated() {
////				Log.d(TAG, "[CAST] onPreloadStatusUpdated()");
//			}
//
//			@Override
//			public void onSendingRemoteMediaRequest() {
//				Log.d(TAG, "[CAST] onSendingRemoteMediaRequest()");
//				Intent intent = new Intent(PlayerActivity.this, CastControllerActivity.class);
//				startActivity(intent);
//			}
//
//			@Override
//			public void onAdBreakStatusUpdated() {
//				Log.d(TAG, "[CAST] onAdBreakStatusUpdated()");
//			}
//		};
//	}
//
//	private void createRemoteMediaClientProgressListener() {
//		mRemoteClientProgressListener = new RemoteMediaClient.ProgressListener() {
//			@Override
//			public void onProgressUpdated(long l, long l1) {
//				mPlayStatus.mPosition = l;
//			}
//		};
//	}
//
//	private void createMessageReceivedCallback() {
//		mMessageReceivedCallback = new Cast.MessageReceivedCallback() {
//			@Override
//			public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
//				Log.d(TAG, "[CAST] message from receiver: " + message);
//				switch (message) {
//					case "PLAYBACK":
//						Toast.makeText(getApplicationContext(), "The receiver can not play the contents. Is it supported contents format?", Toast.LENGTH_LONG).show();
//						break;
//					case "MEDIAKEYS":
//						Toast.makeText(getApplicationContext(), "The receiver can not decrypt the contents. Please check license status", Toast.LENGTH_LONG).show();
//						break;
//					case "NETWORK":
//						Toast.makeText(getApplicationContext(), "The receiver can not find the contents. Please check network status", Toast.LENGTH_LONG).show();
//						break;
//					case "MANIFEST":
//						Toast.makeText(getApplicationContext(), "The receiver can not read the contents' manifest. Please contact contents provider", Toast.LENGTH_LONG).show();
//						break;
//					case "UNKNOWN":
//					default:
//						Toast.makeText(getApplicationContext(), "The receiver reports unknown error. Please try again", Toast.LENGTH_LONG).show();
//						break;
//				}
//
//				releaseCast();
//				finish();
//			}
//		};
//	}
//	private boolean getRemoteMediaClient(CastSession session) {
//		if( session != null )
//			mCastSession = session;
//		return 	getRemoteMediaClient();
//	}
//
//	private boolean getRemoteMediaClient() {
//		if( mRemoteClient != null ) {
//			removeCastReceivers();
//			mRemoteClient = null;
//		}
//
//		mRemoteClient = mCastSession.getRemoteMediaClient();
//		if (mRemoteClient == null) {
//			Log.d(TAG, "[CAST] remote media client is null");
//			return false;
//		}
//
//		addCastReceivers();
//
//		return true;
//	}
//
//	private MediaInfo buildMediaInfo(String source, String title, String subtitle, String thumbImageUrl) {
//		MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
//
//		if( title == null )
//			title = "Content from sender app";
//		if( thumbImageUrl == null )
//			thumbImageUrl = "http://demo.netsync.co.kr/Mob/Cont/images/no_thumb.png";
//
//		movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
//		movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, subtitle);
//		movieMetadata.addImage(new WebImage(Uri.parse(thumbImageUrl)));
//
//		JSONObject castCustomData = new JSONObject();
//		try {
//			Intent intent = getIntent();
//			if( intent.hasExtra(DRM_LICENSE_URL)) {
//				// get license custom data
//				String userid = intent.getStringExtra(DRM_USERID);
//				String cid = intent.getStringExtra(DRM_CID);
//				String oid = intent.getStringExtra(DRM_OID);
//
//				String licenseUrl = intent.getStringExtra(DRM_LICENSE_URL);
//				String customData = PallyconWVMSDKFactory.getInstance(this).getCustomData(userid, cid, oid);
//
//				// input license data for receiver
//				castCustomData.put("ContentProtection", "widevine"); // 'widevine' must be lower case
//
//				// IT DOES NOT WORK
////				castCustomData.put("licenseUrl", licenseUrl);
////				castCustomData.put("licenseCustomData", customData);
//
//				// IT DOES NOT WORK TOO
//				// create json object with 'pallycon-customdata-v2' and license custom data
////				JSONObject licenseCustomData = new JSONObject();
////				licenseCustomData.put("pallycon-customdata-v2", customData);
////				castCustomData.put("licenseUrl", licenseUrl);
////				castCustomData.put("licenseCustomData", licenseCustomData);
//
//				// IT WORKS !!
//				castCustomData.put("licenseUrl", licenseUrl + "?pallycon-customdata-v2=" + customData);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return new MediaInfo.Builder(source)
//				.setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
//				.setContentType("application/dash+xml")
//				.setMetadata(movieMetadata)
//				.setCustomData(castCustomData)
//				.build();
//	}
//
//	private boolean loadRemoteMedia(long position, boolean autoPlay) {
//		Log.d(TAG, "[CAST] loadRemoteMedia()");
//		if (mCastSession == null) {
//			Log.d(TAG, "[CAST] cast session is null");
//			return false;
//		}
//
//		Intent intent = getIntent();
//		Uri uri = intent.getData();
//
//		if( uri == null || uri.toString().length() < 1 ) {
//			Log.d(TAG, "[CAST] uri to cast is invalid");
//			return false;
//		}
//
//		try {
//			MediaLoadOptions options = new MediaLoadOptions.Builder()
//					.setAutoplay(autoPlay)
//					.setPlayPosition(position)
//					.build();
//			if( !getRemoteMediaClient() )
//				return false;
//
//			bNowOnChromecast = true;
//			mRemoteClient.load(buildMediaInfo(uri.toString(), intent.getStringExtra(CONTENTS_TITLE), null, intent.getStringExtra(THUMB_URL)), options);
//			return true;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return false;
//	}
//
//	//// end of Chromecast
}
