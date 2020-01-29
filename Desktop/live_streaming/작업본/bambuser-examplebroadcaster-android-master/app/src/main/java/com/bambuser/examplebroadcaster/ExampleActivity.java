package com.bambuser.examplebroadcaster;

import android.Manifest.permission;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bambuser.broadcaster.BroadcastStatus;
import com.bambuser.broadcaster.Broadcaster;
import com.bambuser.broadcaster.CameraError;
import com.bambuser.broadcaster.ConnectionError;
import com.bambuser.broadcaster.Resolution;
import com.bambuser.broadcaster.TalkbackState;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.OpenChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

public class ExampleActivity extends Activity implements View.OnClickListener, Broadcaster.Observer,
		Broadcaster.TalkbackObserver, Broadcaster.UplinkSpeedObserver, UploadHelper.ProgressCallback,
		Broadcaster.ViewerCountObserver {

	private static String TAG = "ExampleActivity";


	private static final int FILE_CHOOSER_CODE = 1;
	private static final int START_PERMISSIONS_CODE = 2;
	private static final int BROADCAST_PERMISSIONS_CODE = 3;
	private static final int PHOTO_PERMISSIONS_CODE = 4;
	private static final int TALKBACK_DIALOG = 1;
	private static final int UPLOAD_PROGRESS_DIALOG = 2;
	private static final String TALKBACK_DIALOG_CALLER = "caller";
	private static final String TALKBACK_DIALOG_REQUEST = "request";
	private static final String TALKBACK_DIALOG_SESSION_ID = "session_id";
	private static final String STATE_IN_PERMISSION_REQUEST = "in_permission_request";

	private static final String APPLICATION_ID = "XqDkmOHQtkNP9bd5VR6Qvg";

	//센드버드
	private static final String APP_ID = "2651701A-6EE0-4519-A94D-F2286E7AAB01";
	private OpenChannel mChannel;
	private String mChannelUrl;


	private EditText mMessageEditText;
	private Button mMessageSendButton;
	private InputMethodManager mIMM;


	private Context context;


	private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_OPEN_CHAT";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mInPermissionRequest = savedInstanceState != null && savedInstanceState.getBoolean(STATE_IN_PERMISSION_REQUEST);
		setContentView(R.layout.main);
		context = this;
		mDefaultDisplay = getWindowManager().getDefaultDisplay();
		mOrientationListener = new OrientationEventListener(this) {
			@Override
			public void onOrientationChanged(int orientation) {
				if (mBroadcaster != null && mBroadcaster.canStartBroadcasting())
					mBroadcaster.setRotation(mDefaultDisplay.getRotation());
			}
		};
		mBroadcastButton = findViewById(R.id.BroadcastButton);
		mBroadcastButton.setOnClickListener(this);
		mPhotoButton = findViewById(R.id.PhotoButton);
		mPhotoButton.setOnClickListener(this);
		mSwitchButton = findViewById(R.id.SwitchCameraButton);
		mSwitchButton.setOnClickListener(this);
		mFocusButton = findViewById(R.id.FocusButton);
		mFocusButton.setOnClickListener(this);
		mTorchButton = findViewById(R.id.TorchButton);
		mTorchButton.setOnClickListener(this);
		mAboutButton = findViewById(R.id.AboutButton);
		mAboutButton.setOnClickListener(this);
		mTalkbackStopButton = findViewById(R.id.TalkbackStopButton);
		mTalkbackStopButton.setOnClickListener(this);
		mUploadButton = findViewById(R.id.UploadFileButton);
		mUploadButton.setOnClickListener(this);
		mViewerStatus = findViewById(R.id.ViewerStatus);
		mTalkbackStatus = findViewById(R.id.TalkbackStatus);
		mBroadcaster = new Broadcaster(this, APPLICATION_ID, this);
		mBroadcaster.setRotation(mDefaultDisplay.getRotation());
		mBroadcaster.setTalkbackObserver(this);
		mBroadcaster.setUplinkSpeedObserver(this);
		mBroadcaster.setViewerCountObserver(this);
		if (mBroadcaster.getCameraCount() <= 1)
			mSwitchButton.setVisibility(View.INVISIBLE);
		if (!mInPermissionRequest) {
			final List<String> missingPermissions = new ArrayList<>();
			if (!hasPermission(permission.CAMERA))
				missingPermissions.add(permission.CAMERA);
			if (!hasPermission(permission.RECORD_AUDIO))
				missingPermissions.add(permission.RECORD_AUDIO);
			if (!hasPermission(permission.WRITE_EXTERNAL_STORAGE))
				missingPermissions.add(permission.WRITE_EXTERNAL_STORAGE);
			if (missingPermissions.size() > 0)
				requestPermissions(missingPermissions, START_PERMISSIONS_CODE);
		}
		// Set up an ExampleChatController to show incoming messages
		mExampleChatController = new ExampleChatController(this, (ListView) findViewById(R.id.ChatListView), R.layout.chatline, R.id.chat_line_textview, R.id.chat_line_timeview);




		mIMM = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

		SendBird.init(APP_ID, getApplicationContext());
		disconnect();


		// Set up chat box
		mMessageSendButton = (Button) findViewById(R.id.button_open_channel_chat_send);
		mMessageEditText = (EditText) findViewById(R.id.edittext_chat_message);

		mMessageSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendUserMessage(mMessageEditText.getText().toString());
				mExampleChatController.add(mMessageEditText.getText().toString());
				mMessageEditText.setText("");
				mIMM.hideSoftInputFromWindow(mMessageEditText.getWindowToken(), 0);
			}
		});
	}




	private void disconnect() {
		SendBird.unregisterPushTokenAllForCurrentUser(new SendBird.UnregisterPushTokenHandler() {
			@Override
			public void onUnregistered(SendBirdException e) {
				if (e != null) {
					// Error!
					e.printStackTrace();

					// Don't return because we still need to disconnect.
				} else {
//                    Toast.makeText(MainActivity.this, "All push tokens unregistered.", Toast.LENGTH_SHORT).show();
				}

				ConnectionManager.logout(new SendBird.DisconnectHandler() {
					@Override
					public void onDisconnected() {
						try {
							PreferenceUtils.setConnected(false);
						}catch (Exception e){
							e.printStackTrace();
						}
						SendBird.connect("testdr003", new SendBird.ConnectHandler() {
							@Override
							public void onConnected(User user, SendBirdException e) {
								Log.d(TAG,"connect : " );
								if (e != null) {    // Error.
                                    e.printStackTrace();
									Log.d(TAG,"connect : 1" );
									return;
								}else
									createChannel();
							}
						});
						Log.d(TAG,"connect : onDisconnected : " );
					}
				});
			}
		});
	}

	private void createChannel() {
		Log.d(TAG,"connect : createChannel1 : " );
		createChannel2("22944be6-7005-5d5d-47be-ae1d2ab61900");
//		OpenChannel.createChannel(new OpenChannel.OpenChannelCreateHandler() {
//			@Override
//			public void onResult(OpenChannel openChannel, SendBirdException e) {
//				Log.d(TAG,"connect : createChannel2 : " + openChannel.getUrl());
//
//				if (e != null) {    // Error.
//					return;
//				}
//				else
//					getChannel();
//			}
//		});
	}
	private void createChannel2(String name) {
		OpenChannelListQuery channelListQuery = OpenChannel.createOpenChannelListQuery();
		channelListQuery.setNameKeyword(name);

		channelListQuery.next(new OpenChannelListQuery.OpenChannelListQueryResultHandler() {
			@Override
			public void onResult(List<OpenChannel> openChannels, SendBirdException e) {
				Log.d(TAG,"connect : createChannel2 size : " + openChannels.size());
				if(openChannels.size() > 0){
					Log.d(TAG,"connect : createChannel2 : " + openChannels.get(0).getUrl());
					getChannel(openChannels.get(0).getUrl());
				}else{
					createChannel3("22944be6-7005-5d5d-47be-ae1d2ab61900");
				}
				for(int i = 0;i < openChannels.size();i++)
					Log.d(TAG,"connect : createChannel2 : " + openChannels.get(i).getUrl());
				if (e != null) {    // Error.
					return;
				}


				// A list of open channels that have "SendBird" in their names is returned.
			}
		});
	}
	private void createChannel3(String name) {
		Log.d(TAG,"connect : createChannel3 size : " + name);
		OpenChannel.createChannel(name, "", "", "", null, new OpenChannel.OpenChannelCreateHandler() {
			@Override
			public void onResult(OpenChannel openChannel, SendBirdException e) {
				if (e != null) {    // Error.
					return;
				}
				Log.d(TAG,"connect : createChannel3 size : " + openChannel.getUrl());
				getChannel(openChannel.getUrl());
			}
		});
	}

	private void getChannel(String channelUrl){

		mChannelUrl = channelUrl;
		OpenChannel.getChannel(mChannelUrl, new OpenChannel.OpenChannelGetHandler() {
			@Override
			public void onResult(final OpenChannel openChannel, SendBirdException e) {
				Log.d(TAG,"connect : getChannel 1 : " );
				if (e != null) {    // Error.
					return;
				}
				Log.d(TAG,"connect : getChannel 2 : " );
				openChannel.enter(new OpenChannel.OpenChannelEnterHandler() {
					@Override
					public void onResult(SendBirdException e) {
						Log.d(TAG,"connect : getChannel 3 : " );
						if (e != null) {    // Error.
                            e.printStackTrace();
                            Log.d(TAG,"connect : getChannel 31 : " );
							return;
						}
                        mChannel = openChannel;
                        Log.d(TAG,"connect : getChannel 4 : " );
						openChannel.enter(new OpenChannel.OpenChannelEnterHandler() {
							@Override
							public void onResult(SendBirdException e) {
								if (e != null) {
									// Error!
									e.printStackTrace();
									return;
								}

								mChannel = openChannel;


							}
						});
					}
				});
			}
		});
	}

	private void sendUserMessage(String text) {
		mChannel.sendUserMessage(text, new BaseChannel.SendUserMessageHandler() {
			@Override
			public void onSent(UserMessage userMessage, SendBirdException e) {
				if (e != null) {
					// Error!

					Toast.makeText(
							context,
							"Send failed with error " + e.getCode() + ": " + e.getMessage(), Toast.LENGTH_SHORT)
							.show();
					return;
				}

				// Display sent message to RecyclerView
//				mChatAdapter.addFirst(userMessage);
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mBroadcaster.onActivityDestroy();
		mExampleChatController = null;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (outState != null)
			outState.putBoolean(STATE_IN_PERMISSION_REQUEST, mInPermissionRequest);
	}

	@Override
	public void onPause() {
		super.onPause();
		mOrientationListener.disable();
		mBroadcaster.onActivityPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		mOrientationListener.enable();
		mBroadcaster.setCameraSurface((SurfaceView) findViewById(R.id.PreviewSurfaceView));
		mBroadcaster.setRotation(mDefaultDisplay.getRotation());
		mBroadcaster.onActivityResume();


		SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
			@Override
			public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
				Log.d(TAG,"connect : onMessageReceived 1");
				// Add new message to view
				if (baseChannel.getUrl().equals(mChannelUrl)) {
					Log.d(TAG,"connect : onMessageReceived 2");
					mExampleChatController.show();
					mExampleChatController.add(((UserMessage) baseMessage).getMessage());
				}
			}

			@Override
			public void onMessageDeleted(BaseChannel baseChannel, long msgId) {
				super.onMessageDeleted(baseChannel, msgId);
				if (baseChannel.getUrl().equals(mChannelUrl)) {

				}
			}

			@Override
			public void onMessageUpdated(BaseChannel channel, BaseMessage message) {
				super.onMessageUpdated(channel, message);
				if (channel.getUrl().equals(mChannelUrl)) {

				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.BroadcastButton:
			if (mBroadcaster.canStartBroadcasting()) {
				if (hasPermission(permission.CAMERA) && hasPermission(permission.RECORD_AUDIO)) {
					lockCurrentOrientation();
					initLocalRecording();
					mBroadcaster.startBroadcast();
				} else {
					List<String> permissions = new ArrayList<>();
					if (!hasPermission(permission.CAMERA))
						permissions.add(permission.CAMERA);
					if (!hasPermission(permission.RECORD_AUDIO))
						permissions.add(permission.RECORD_AUDIO);
					if (!hasPermission(permission.WRITE_EXTERNAL_STORAGE))
						permissions.add(permission.WRITE_EXTERNAL_STORAGE);
					requestPermissions(permissions, BROADCAST_PERMISSIONS_CODE);
				}
			} else
				mBroadcaster.stopBroadcast();
			break;
		case R.id.PhotoButton:
			takePhoto();
			break;
		case R.id.FocusButton:
			mBroadcaster.focus();
			break;
		case R.id.TorchButton:
			mBroadcaster.toggleTorch();
			break;
		case R.id.AboutButton:
			startActivity(new Intent(this, AboutActivity.class));
			break;
		case R.id.UploadFileButton:
			chooseFile();
			break;
		case R.id.SwitchCameraButton:
			mBroadcaster.switchCamera();
			break;
		case R.id.TalkbackStopButton:
			if (mExampleChatController != null) {
				count++;
				mExampleChatController.add("aaa : " + count);
				mExampleChatController.show();
			}
//			mBroadcaster.stopTalkback();
			break;
		}
	}

	@Override
	public void onActivityResult(int code, int result, Intent data) {
		if (code == FILE_CHOOSER_CODE) {
			if (result == Activity.RESULT_OK && data != null && data.getData() != null)
				startUpload(data.getData());
			else
				Toast.makeText(getApplicationContext(), "no file chosen", Toast.LENGTH_SHORT).show();
		}
		super.onActivityResult(code, result, data);
	}

	private void requestPermissions(List<String> missingPermissions, int code) {
		mInPermissionRequest = true;
		String[] permissions = missingPermissions.toArray(new String[missingPermissions.size()]);
		try {
			getClass().getMethod("requestPermissions", String[].class, Integer.TYPE).invoke(this, permissions, code);
		} catch (Exception ignored) {}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		mInPermissionRequest = false;
		if (requestCode == START_PERMISSIONS_CODE) {
			if (!hasPermission(permission.CAMERA) || !hasPermission(permission.RECORD_AUDIO))
				Toast.makeText(getApplicationContext(), "Missing permission to camera or audio", Toast.LENGTH_SHORT).show();
		} else if (requestCode == BROADCAST_PERMISSIONS_CODE) {
			if (hasPermission(permission.CAMERA) && hasPermission(permission.RECORD_AUDIO)) {
				lockCurrentOrientation();
				initLocalRecording();
				mBroadcaster.startBroadcast();
			} else
				Toast.makeText(getApplicationContext(), "Missing permission to camera or audio", Toast.LENGTH_SHORT).show();
		} else if (requestCode == PHOTO_PERMISSIONS_CODE) {
			if (!hasPermission(permission.CAMERA) || !hasPermission(permission.WRITE_EXTERNAL_STORAGE))
				Toast.makeText(getApplicationContext(), "Missing permission to camera or storage", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		if (id == TALKBACK_DIALOG) {
			return new AlertDialog.Builder(this).setTitle("Talkback request pending")
			.setCancelable(false)
			.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					mBroadcaster.stopTalkback();
				}
			})
			.setPositiveButton("Accept", null)
			.setMessage("Incoming talkback call")
			.create();
		} else if (id == UPLOAD_PROGRESS_DIALOG) {
			mUploadDialog = new AlertDialog.Builder(this).setTitle("Uploading")
			.setView(getLayoutInflater().inflate(R.layout.upload_progress_dialog, null))
			.setCancelable(false)
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					mUploading = false;
				}
			})
			.create();
			return mUploadDialog;
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(final int id, final Dialog dialog, final Bundle args) {
		if (id == TALKBACK_DIALOG) {
			final String caller = args.getString(TALKBACK_DIALOG_CALLER);
			final String request = args.getString(TALKBACK_DIALOG_REQUEST);
			final int sessionId = args.getInt(TALKBACK_DIALOG_SESSION_ID);
			AlertDialog ad = (AlertDialog) dialog;
			ad.setButton(DialogInterface.BUTTON_POSITIVE, "Accept", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface d, int which) {
					mBroadcaster.acceptTalkback(sessionId);
				}
			});
			String msg = "Incoming talkback call";
			if (caller != null && caller.length() > 0)
				msg += " from: " + caller;
			if (request != null && request.length() > 0)
				msg += ": " + request;
			msg += "\nPlease plug in your headphones and accept, or reject the call.";
			ad.setMessage(msg);
		} else if (id == UPLOAD_PROGRESS_DIALOG) {
			((ProgressBar)dialog.findViewById(R.id.UploadProgressBar)).setProgress(0);
			((TextView)dialog.findViewById(R.id.UploadStatusText)).setText("Connecting...");
		}
		super.onPrepareDialog(id, dialog);
	}

	private void takePhoto() {
		List<String> missingPermissions = new ArrayList<>();
		if (!hasPermission(permission.CAMERA))
			missingPermissions.add(permission.CAMERA);
		if (!hasPermission(permission.WRITE_EXTERNAL_STORAGE))
			missingPermissions.add(permission.WRITE_EXTERNAL_STORAGE);
		if (missingPermissions.size() > 0) {
			requestPermissions(missingPermissions, PHOTO_PERMISSIONS_CODE);
			return;
		}
		List<Resolution> resolutions = mBroadcaster.getSupportedPictureResolutions();
		if (resolutions.isEmpty())
			return;
		Resolution maxRes = resolutions.get(resolutions.size()-1);
		Broadcaster.PictureObserver observer = new Broadcaster.PictureObserver() {
			@Override public void onPictureStored(File file) {
				Toast.makeText(getApplicationContext(), "Stored " + file.getName(), Toast.LENGTH_SHORT).show();
				MediaScannerConnection.scanFile(getApplicationContext(), new String[] {file.getAbsolutePath()}, null, null);
			}
		};
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS", Locale.US);
		String fileName = sdf.format(new Date()) + ".jpg";
		File storageDir = getStorageDir();
		if (storageDir == null) {
			Toast.makeText(getApplicationContext(), "Can't store picture, external storage unavailable", Toast.LENGTH_LONG).show();
			return;
		}
		File file = new File(storageDir, fileName);
		mBroadcaster.takePicture(file, maxRes, observer);
	}

	private void initLocalRecording() {
		if (mBroadcaster == null || !mBroadcaster.hasLocalMediaCapability())
			return;
		File storageDir = getStorageDir();
		if (storageDir == null) {
			Toast.makeText(getApplicationContext(), "Can't store local copy, external storage unavailable", Toast.LENGTH_SHORT).show();
			return;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
		String fileName = sdf.format(new Date()) + ".mp4";
		File file = new File(storageDir, fileName);
		Broadcaster.LocalMediaObserver obs = new Broadcaster.LocalMediaObserver() {
			public void onLocalMediaError() {
				Toast.makeText(getApplicationContext(), "Failed to write to video file. Storage/memory full?", Toast.LENGTH_LONG).show();
			}
			public void onLocalMediaClosed(String filePath) {
				if (filePath != null && filePath.endsWith(".mp4")) {
					Toast.makeText(getApplicationContext(), "Local copy of broadcast stored", Toast.LENGTH_SHORT).show();
					MediaScannerConnection.scanFile(getApplicationContext(), new String[] {filePath}, null, null);
				}
			}
		};
		boolean success = mBroadcaster.storeLocalMedia(file, obs);
		Toast.makeText(getApplicationContext(), "Writing to " + file.getAbsolutePath() + (success ? "" : " failed"), Toast.LENGTH_SHORT).show();
	}

	private File getStorageDir() {
		if (hasPermission(permission.WRITE_EXTERNAL_STORAGE) && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			final File externalDir = new File(Environment.getExternalStorageDirectory(), "LibBambuser");
			externalDir.mkdirs();
			if (externalDir.exists() && externalDir.canWrite())
				return externalDir;
		}
		return null;
	}

	private boolean hasPermission(String permission) {
		try {
			int result = (Integer) getClass().getMethod("checkSelfPermission", String.class).invoke(this, permission);
			return result == PackageManager.PERMISSION_GRANTED;
		} catch (Exception ignored) {}
		return true;
	}

	private void chooseFile() {
		Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
		chooseFileIntent.setType("*/*");
		chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
		try {
			startActivityForResult(chooseFileIntent, FILE_CHOOSER_CODE);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "No activity that could choose file", Toast.LENGTH_SHORT).show();
		}
	}

	private void startUpload(Uri uri) {
		getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
		mUploading = true;
		showDialog(UPLOAD_PROGRESS_DIALOG);
		UploadHelper.upload(this, uri, APPLICATION_ID, "", "My uploaded file example", null, this);
	}

	private void lockCurrentOrientation() {
		int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
		int configOrientation = getResources().getConfiguration().orientation;
		int screenOrientation = getScreenOrientation(displayRotation, configOrientation);
		setRequestedOrientation(screenOrientation);
	}

	private static int getScreenOrientation(int displayRotation, int configOrientation) {
		if (configOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			if (displayRotation == Surface.ROTATION_0 || displayRotation == Surface.ROTATION_90)
				return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			else
				return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
		} else {
			if (displayRotation == Surface.ROTATION_0 || displayRotation == Surface.ROTATION_270)
				return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			else
				return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
		}
	}

	static public class AboutActivity extends Activity {
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			WebView webView = new WebView(this);
			webView.loadUrl("file:///android_asset/licenses.html");
			// WebViewClient necessary since Android N to handle links in the license document
			webView.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					return false;
				}
			});
			setContentView(webView);
		}
	}

	@Override
	public void onConnectionStatusChange(final BroadcastStatus status) {
		if (status == BroadcastStatus.STARTING)
			getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
		if (status == BroadcastStatus.IDLE)
			getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
		if (status == BroadcastStatus.IDLE)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		if (status == BroadcastStatus.IDLE)
			mViewerStatus.setText("");
		mBroadcastButton.setText(status == BroadcastStatus.IDLE ? "Broadcast" : "Disconnect");
		mSwitchButton.setEnabled(status == BroadcastStatus.IDLE);
	}

	@Override
	public void onStreamHealthUpdate(final int health) {
	}

	@Override
	public void onConnectionError(final ConnectionError type, final String message) {
		String str = type.toString();
		if (message != null)
			str += " " + message;
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onCameraError(final CameraError error) {
		Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
	}

	@Override
	public void onChatMessage(final String message) {
		Log.d(TAG,"onChatMessage " + message);
		if (mExampleChatController != null) {
			mExampleChatController.add(message);
			mExampleChatController.show();
		}
	}

	@Override
	public void onResolutionsScanned() {
		// invoking setResolution() in this callback at every camera change, to possibly switch to a higher resolution.
		mBroadcaster.setResolution(0, 0);
	}

	@Override
	public void onCameraPreviewStateChanged() {
		mTorchButton.setEnabled(mBroadcaster.hasTorch());
		mFocusButton.setEnabled(mBroadcaster.hasFocus());
	}

	@Override
	public void onBroadcastInfoAvailable(String videoId, String url) {
	}

	@Override
	public void onBroadcastIdAvailable(String broadcastId) {
		Toast.makeText(getApplicationContext(), "Broadcast id " + broadcastId + " published", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onTalkbackStateChanged(final TalkbackState state, final int id, final String caller, final String request) {
		try {
			removeDialog(TALKBACK_DIALOG);
		} catch (Exception ignored) {}
		switch (state) {
		case IDLE:
			mTalkbackStopButton.setVisibility(View.GONE);
			mTalkbackStatus.setText("");
			break;
		case NEEDS_ACCEPT:
			mTalkbackStatus.setText("talkback pending");
			Bundle args = new Bundle();
			args.putInt(TALKBACK_DIALOG_SESSION_ID, id);
			args.putString(TALKBACK_DIALOG_CALLER, caller);
			args.putString(TALKBACK_DIALOG_REQUEST, request);
			showDialog(TALKBACK_DIALOG, args);
			break;
		case ACCEPTED:
		case READY:
			mTalkbackStopButton.setVisibility(View.VISIBLE);
			mTalkbackStatus.setText("talkback connecting");
			break;
		case PLAYING:
			mTalkbackStatus.setText("talkback active");
			break;
		}
	}

	@Override
	public void onUplinkTestComplete(long bitrate, boolean recommendation) {
		String toast = "Uplink test complete, bandwidth: " + bitrate/1024 + " kbps, broadcasting recommended: " + recommendation;
		Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onProgress(final long currentBytes, final long totalBytes) {
		if (currentBytes == totalBytes || System.currentTimeMillis() > mLastUploadStatusUpdateTime + 500) {
			mLastUploadStatusUpdateTime = System.currentTimeMillis();
			runOnUiThread(new Runnable() { @Override public void run() {
				if (mUploadDialog == null)
					return;
				int permille = (int) (currentBytes * 1000 / totalBytes);
				((ProgressBar)mUploadDialog.findViewById(R.id.UploadProgressBar)).setProgress(permille);
				String status = "Sent " + currentBytes/1024 + " KB / " + totalBytes/1024 + " KB";
				((TextView)mUploadDialog.findViewById(R.id.UploadStatusText)).setText(status);
			}});
		}
		return mUploading;
	}

	@Override
	public void onSuccess(final String fileName) {
		runOnUiThread(new Runnable() { @Override public void run() {
			Toast.makeText(getApplicationContext(), "Upload of " + fileName + " completed", Toast.LENGTH_SHORT).show();
			mUploadDialog = null;
			try {
				removeDialog(UPLOAD_PROGRESS_DIALOG);
			} catch (Exception ignored) {}
			getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
		}});
	}

	@Override
	public void onError(final String error) {
		runOnUiThread(new Runnable() { @Override public void run() {
			Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
			mUploadDialog = null;
			try {
				removeDialog(UPLOAD_PROGRESS_DIALOG);
			} catch (Exception ignored) {}
			getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
		}});
	}

	@Override
	public void onCurrentViewersUpdated(long viewers) {
		mViewerStatus.setText("Viewers: " + viewers);
	}

	@Override
	public void onTotalViewersUpdated(long viewers) {
	}

	private boolean mInPermissionRequest = false;
	private Display mDefaultDisplay;
	private OrientationEventListener mOrientationListener;
	private Broadcaster mBroadcaster;
	private Button mBroadcastButton, mPhotoButton, mAboutButton, mSwitchButton, mFocusButton, mTorchButton, mUploadButton;
	private TextView mTalkbackStatus;
	private TextView mViewerStatus;
	private Button mTalkbackStopButton;
	private ExampleChatController mExampleChatController;
	private AlertDialog mUploadDialog;

	private int count = 0;
	private long mLastUploadStatusUpdateTime = 0;
	private boolean mUploading = false;

}
