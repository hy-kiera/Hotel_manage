package com.bambuser.examplebroadcaster;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.bambuser.broadcaster.BroadcastPlayer;
import com.bambuser.broadcaster.PlayerState;
import com.bambuser.broadcaster.SurfaceViewWithAutoAR;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.util.helper.log.Logger;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.OpenChannelListQuery;
import com.sendbird.android.PreviousMessageListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//import com.kakao.kakaolink.KakaoLink;
//import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
//import com.kakao.util.KakaoParameterException;
//import com.kakao.kakaolink.internal.*;
//import com.kakao.kakaolink.v2.KakaoLinkCallback;
//import com.kakao.kakaolink.v2.KakaoLinkResponse;
//import com.kakao.kakaolink.v2.KakaoLinkService;
//import com.kakao.kakaolink.v2.network.*;
//import com.kakao.kakaolink.v2.*;

public class ExamplePlayerActivity extends AppCompatActivity {
	private static final String APPLICATION_ID = "XqDkmOHQtkNP9bd5VR6Qvg";
	private static final String API_KEY = "alcytwrdzc7pue32ppnl1kbrw";


//	private static final String APPLICATION_ID = "Cf8bhZ4bMl3rxv4R2D4g0g";
//	private static final String API_KEY = "bilg4sjnieo7t631swk2j95m6";

    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_OPEN_CHAT";
	private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_OPEN_CHAT";

	private static final int TALKBACK_DIALOG = 1;
	private static final String TALKBACK_DIALOG_CALLER = "caller";
	private static final String TALKBACK_DIALOG_REQUEST = "request";
	private static final String TALKBACK_DIALOG_SESSION_ID = "session_id";

	private static final String APP_ID = "2651701A-6EE0-4519-A94D-F2286E7AAB01";
	private OpenChannel mChannel;
	private String mChannelUrl = "sendbird_open_channel_62570_68260371ef0ede1648b58114a06059a0865af934";

	private static final int CHANNEL_LIST_LIMIT = 30;

	private EditText mMessageEditText;
	private Button mMessageSendButton;
	private Button FollowButton;	//팔로우버튼
	private InputMethodManager mIMM;

	private PopupWindow mPopupWindow ;


	public boolean isChatGone = false;

	private static String TAG = "ExamplePlayerActivity";



	private final Handler mMainHandler = new Handler();
	private final OkHttpClient mOkHttpClient = new OkHttpClient();


	//	private ArrayList<BroadcastPlayer> mAryBroadcastPlayer = new ArrayList<>();
	private ArrayList<View> mAryView = new ArrayList<>();
	private ArrayList<SurfaceViewWithAutoAR> mAryVideoSurfaceView = new ArrayList<>();
	private ArrayList<BroadcastPlayer> mAryBroadcastPlayer = new ArrayList<>();

	private BroadcastPlayer mBroadcastPlayer = null;
//	private MediaController mMediaController = null;
    private ExampleChatController mExampleChatController;

	private ArrayList<String> mPlayList = new ArrayList<>();
	private ArrayList<String> mPlaypreviewList = new ArrayList<>();

	private int nowposition = 0;

	private int playcount = 0;

	private Context context;

	private CustomViewPager viewPager ;
//	private ViewPager viewPager ;
	private ViewPagerAdapter pagerAdapter ;

	public View mRl_Live;

	SectionPageAdapter adapter = new SectionPageAdapter(getSupportFragmentManager());




	//신고 선택 구분용 변수
	//0:음란물, 1:욕설, 2:폭력, 3:부적절한 상품, 4:저작권, 5:기타
	public int select = 0;
	//신고 사유 기술용 변수
	TextView txt_dummy;
	public String txt_dummy_save;




	private int count = 0;
	// 로띠 애니메이션뷰 선언
	LottieAnimationView songLikeAnimButton;
	//LottieAnimationView Clicker01;
	// 좋아요 클릭 여부 확인용 텍스트뷰 선언
	TextView isSongLikeAnimButtonClickedTextView;
	ImageView ClickIcon;
	// 좋아요 클릭 여부
	boolean isSongLikedClicked = false;
	private int is_follow = 0;




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.main_layout);
		onCreateHeart();

		//신고 기술용 팝업 변수
		txt_dummy=(TextView)findViewById(R.id.txt_input);

//		setContentView(R.layout.activity_example_player);

		init();
//		init2();
	}

	public void setupViewPager(ViewPager viewPager) {
		Log.d(TAG,"setupViewPager");
		adapter.clearFragment();

		for(int i = 0 ;i < mPlayList.size();i++) {
			adapter.addFragment(new Fragment_player(), "" + i);
		}
		viewPager.setAdapter(adapter);
		playStart(0,mPlayList.get(0),mPlaypreviewList.get(0));
	}

	private void init(){

		mRl_Live = findViewById(R.id.rl_Live);

		if(isChatGone)
			mRl_Live.setVisibility(View.GONE);

//		mAryVideoSurfaceView.add(null);
//		mAryVideoSurfaceView.add(null);
//		mAryVideoSurfaceView.add(null);
//		mAryView.add(null);
//		mAryView.add(null);
//		mAryView.add(null);




		mExampleChatController = new ExampleChatController(this, (ListView) findViewById(R.id.ChatListView), R.layout.chatline, R.id.chat_line_textview, R.id.chat_line_timeview);
		mExampleChatController.show();
		mExampleChatController.add("test");
		viewPager = (CustomViewPager) findViewById(R.id.viewPager) ;
//		viewPager = (ViewPager) findViewById(R.id.viewPager) ;
		getLatestResourceUri();
//		pagerAdapter = new ViewPagerAdapter(this) ;
//		viewPager.setAdapter(pagerAdapter) ;
		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int i, float v, int i1) {
				Log.d(TAG,"onPageScrolled " + i);
			}

			@Override
			public void onPageSelected(int i) {
				Log.d(TAG,"onPageSelected " + i);

				nowposition = i;
				if(mPlayList.size() > i)
					playStart(i,mPlayList.get(i),mPlaypreviewList.get(i));

			}

			@Override
			public void onPageScrollStateChanged(int i) {
				Log.d(TAG,"onPageScrollStateChanged " + i);
				try {
					//((Fragment_player)(adapter.getFragmentList().get(nowposition))).showPreview();
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		});

		//SendBird

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

		FollowButton = (Button) findViewById(R.id.followButton);
		FollowButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(is_follow == 0){//팔로우 안한 상태에서 클릭하면
					FollowButton.setText("팔로우");
					is_follow = 1;//팔로우 상태로 바꿈
				}
				else{//팔로우 한 상태에서 클릭하면
					FollowButton.setText("팔로우 취소");
					is_follow = 0;//팔로우 취소로 바꿈
				}
			}
		});


//		((Button) findViewById(R.id.button_edit_popup)).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				openEditPopup();
////				Intent intent = new Intent(context, EditPopupActivity.class);
////				startActivity(intent);
//			}
//		});
	}


//	public void shareKakao() {
//
//
//	}


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
						SendBird.connect("testdr", new SendBird.ConnectHandler() {
							@Override
							public void onConnected(User user, SendBirdException e) {
								Log.d(TAG,"connect : " );
								if (e != null) {    // Error.
									Log.d(TAG,"connect : 1" );

									return;
								}else
									//createChannel();
									getChannel(mChannelUrl);

							}
						});
						Log.d(TAG,"connect : onDisconnected : " );
					}
				});
			}
		});
	}

	private void openEditPopup(){
		View popupView = getLayoutInflater().inflate(R.layout.popup_edit_layout, null);
		mPopupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		//popupView 에서 (LinearLayout 을 사용) 레이아웃이 둘러싸고 있는 컨텐츠의 크기 만큼 팝업 크기를 지정

		mPopupWindow.setFocusable(true);
		// 외부 영역 선택히 PopUp 종료

		mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
	}

	private void createChannel() {
		onResume();
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
				Log.d(TAG,"connect : getChannel 1 : " + mChannelUrl);
				if (e != null) {    // Error.
					return;
				}
				Log.d(TAG,"connect : getChannel 2 : " + mChannelUrl);
				openChannel.enter(new OpenChannel.OpenChannelEnterHandler() {
					@Override
					public void onResult(SendBirdException e) {
						Log.d(TAG,"connect : getChannel 3 : " + mChannelUrl);
						if (e != null) {    // Error.
							e.printStackTrace();
							Log.d(TAG,"connect : getChannel 31 : " + mChannelUrl);
							return;
						}
						Log.d(TAG,"connect : getChannel 32 : " + mChannelUrl);
						mChannel = openChannel;
//						openChannel.enter(new OpenChannel.OpenChannelEnterHandler() {
//							@Override
//							public void onResult(SendBirdException e) {
//								if (e != null) {
//									// Error!
//									e.printStackTrace();
//									return;
//								}
//								Log.d(TAG,"connect : getChannel 33 : " + mChannelUrl);
//								mChannel = openChannel;
//
//
//							}
//						});
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



	public class ViewPagerAdapter extends PagerAdapter {

		// LayoutInflater 서비스 사용을 위한 Context 참조 저장.
		private Context mContext = null ;

		public ViewPagerAdapter() {

		}

		// Context를 전달받아 mContext에 저장하는 생성자 추가.
		public ViewPagerAdapter(Context context) {
			mContext = context ;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Log.d(TAG,"ViewPagerAdapter instantiateItem "+ position);
			View view = null ;


			if (mContext != null) {
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.fragment_player, container, false);
				mAryView.add(position%3,view);
				mAryVideoSurfaceView.add(position%3,(SurfaceViewWithAutoAR) mAryView.get(position%3).findViewById(R.id.VideoSurfaceView));
			}



			// 뷰페이저에 추가.
			container.addView(mAryView.get(position%3)) ;

			return view ;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			Log.d(TAG,"ViewPagerAdapter destroyItem "+ position);
			// 뷰페이저에서 삭제.
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			// 전체 페이지 수는 10개로 고정.
			return 10;
		}

		@Override
		public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
			Log.d(TAG,"ViewPagerAdapter isViewFromObject ");
			return (view == (View)object);
		}
	}

	private void init2(){
//		mVideoSurfaceView = findViewById(R.id.VideoSurfaceView);
	}

	private void refresh() {
		loadInitialMessageList(CHANNEL_LIST_LIMIT);
	}

	private void loadInitialMessageList(int numMessages) {

		PreviousMessageListQuery mPrevMessageListQuery = mChannel.createPreviousMessageListQuery();
		mPrevMessageListQuery.load(numMessages, true, new PreviousMessageListQuery.MessageListQueryResult() {
			@Override
			public void onResult(List<BaseMessage> list, SendBirdException e) {
				if (e != null) {
					// Error!
					e.printStackTrace();
					return;
				}

				//mChatAdapter.setMessageList(list);
			}
		});

	}

	private void refreshFirst() {
		getChannel(mChannelUrl);
	}

	private void setPreViewVisibility(int position,int show){
		//getPreView(position).setVisibility(show);
	}

	private ImageView getPreView(int position){
		return (ImageView) mAryView.get(position%3).findViewById(R.id.img_preview);
	}

	private void setImgUrlInPreview(int position){
//		Picasso.with(context).load(mPlaypreviewList.get(position)).into((getPreView(position)));
	}




	@Override
	protected void onResume() {
		super.onResume();

//		if(!isChatGone){
//			return;
//		}
		Log.d(TAG,"connect : onResume 1");
//		mVideoSurfaceView = findViewById(R.id.VideoSurfaceView);


		try {
//			ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID, new ConnectionManager.ConnectionManagementHandler() {
//				@Override
//				public void onConnected(boolean reconnect) {
//					if (reconnect) {
//						refresh();
//					} else {
//						refreshFirst();
//					}
//				}
//			});
//			SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
//			SendBird.removeAllChannelHandlers();

			SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
				@Override
				public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
					Log.d(TAG, "connect : onMessageReceived 1" + mChannelUrl);
					// Add new message to view
					if (baseChannel.getUrl().equals(mChannelUrl)) {
						Log.d(TAG, "connect : onMessageReceived 2");
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
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(!isChatGone){
			return;
		}

//		mOkHttpClient.dispatcher().cancelAll();
//		setLatencyTimer(false);
//		if (mBroadcastPlayer != null)
//			mBroadcastPlayer.close();
//		mBroadcastPlayer = null;
//		mVideoSurfaceView = null;
//		if (mMediaController != null)
//			mMediaController.hide();
//		mMediaController = null;
//		if (mBroadcastLiveTextView != null)
//			mBroadcastLiveTextView.setVisibility(View.GONE);
		Log.d(TAG,"connect : onPause 1");
		ConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID);
		SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
//		getMenuInflater().inflate(R.menu.main_options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.about_menu_item:
				startActivity(new Intent(this, AboutActivity.class));
				return true;

			//카카오 공유 기능
			case R.id.menu_share:
				//카카오 공유
//				shareKakao();
				// 기본적인 스크랩 템플릿을 사용하여 보내는 코드
				Map<String, String> serverCallbackArgs = new HashMap<String, String>();
				serverCallbackArgs.put("user_id", "${current_user_id}");
				serverCallbackArgs.put("product_id", "${shared_product_id}");

				KakaoLinkService.getInstance().sendScrap(this, "https://developers.kakao.com", serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>() {
					@Override
					public void onFailure(ErrorResult errorResult) {
						Logger.e(errorResult.toString());
					}

					@Override
					public void onSuccess(KakaoLinkResponse result) {
						// 템플릿 밸리데이션과 쿼터 체크가 성공적으로 끝남. 톡에서 정상적으로 보내졌는지 보장은 할 수 없다. 전송 성공 유무는 서버콜백 기능을 이용하여야 한다.
					}
				});
				break;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public boolean onTouchEvent(MotionEvent ev) {
//		if (ev.getActionMasked() == MotionEvent.ACTION_UP && mBroadcastPlayer != null && mMediaController != null) {
//			PlayerState state = mBroadcastPlayer.getState();
//			if (state == PlayerState.PLAYING ||
//				state == PlayerState.BUFFERING ||
//				state == PlayerState.PAUSED ||
//				state == PlayerState.COMPLETED) {
//				if (mMediaController.isShowing())
//					mMediaController.hide();
//				else
//					mMediaController.show();
//			} else {
//				mMediaController.hide();
//			}
//		}
		return false;
	}

	private void getLatestResourceUri() {
		Request request = new Request.Builder()
			.url("https://api.bambuser.com/broadcasts")
			.addHeader("Accept", "application/vnd.bambuser.v1+json")
			.addHeader("Content-Type", "application/json")
			.addHeader("Authorization", "Bearer " + API_KEY)
			.get()
			.build();
		mOkHttpClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(final Call call, final IOException e) {
				runOnUiThread(new Runnable() { @Override public void run() {

				}});
			}
			@Override
			public void onResponse(final Call call, final Response response) throws IOException {
				String resourceUri = null;
				String body= null;
				try {
					body = response.body().string();
					JSONObject json = new JSONObject(body);
					JSONArray results = json.getJSONArray("results");
					JSONObject latestBroadcast = results.optJSONObject(0);
					resourceUri = latestBroadcast.optString("resourceUri");
				} catch (Exception ignored) {}
				final String uri = resourceUri;
				final String allbody = body;
				runOnUiThread(new Runnable() { @Override public void run() {
					initPlayer(uri,allbody);
				}});
			}
		});
	}

	private void initPlayer(String resourceUri,String body) {
		if(!body.isEmpty()){
			try {
				mPlayList.clear();
				mPlaypreviewList.clear();
				JSONObject json = new JSONObject(body);
				JSONArray results = json.getJSONArray("results");
				for(int i = 0 ; i < results.length();i++){
					JSONObject latestBroadcast = results.optJSONObject(i);
					resourceUri = latestBroadcast.optString("resourceUri");
					resourceUri = latestBroadcast.optString("resourceUri");
					Log.d(TAG,"initPlayer resourceUri : "+i+" : " + resourceUri);
					mPlayList.add(resourceUri);
					mPlaypreviewList.add(latestBroadcast.optString("preview"));
				}

			} catch (Exception ignored) {

			}

		}

		if (resourceUri == null) {
			return;
		}


		setupViewPager(viewPager);
	}

	private void playStart(final int position, final String resourceUri,final String previewUri) {
//		if(position > 0){
//			((Fragment_player)(adapter.getFragmentList().get(position))).playStart(resourceUri,APPLICATION_ID);
//			return;
//		}
		((Fragment_player)(adapter.getFragmentList().get(position))).playStart(resourceUri,APPLICATION_ID,previewUri);

//		SurfaceViewWithAutoAR videoView = ((Fragment_player)(adapter.getFragmentList().get(position))).getVideoSurfaceView();
//
//		if (videoView == null) {
//			return;
//		}
//
//
//
////		setPreViewVisibility(position,View.VISIBLE);
////		setImgUrlInPreview(position);
////		SurfaceViewWithAutoAR videoSurfaceView = (SurfaceViewWithAutoAR) videoView;
//
//		if (mBroadcastPlayer != null)
//			mBroadcastPlayer.close();
//
//		Log.d(TAG,"playStart4 : "+ position) ;
//		mBroadcastPlayer = null;
//		mBroadcastPlayer = new BroadcastPlayer(this, resourceUri, APPLICATION_ID, mPlayerObserver);
//
//		mBroadcastPlayer.setSurfaceView(videoView);
//		mBroadcastPlayer.setAcceptType(BroadcastPlayer.AcceptType.ANY);
//		mBroadcastPlayer.setViewerCountObserver(mViewerCountObserver);
//		Log.d(TAG,"playStart5 : "+ position);
//		mBroadcastPlayer.load();
	}

	public static class AboutActivity extends Activity {
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

	private void updateVolume(float progress) {
		// Output volume should optimally increase logarithmically, but Android media player APIs
		// respond linearly. Producing non-linear scaling between 0.0 and 1.0 by using x^4.
		// Not exactly logarithmic, but has the benefit of satisfying the end points exactly.
//		if (mBroadcastPlayer != null)
//			mBroadcastPlayer.setAudioVolume(progress * progress * progress * progress);
	}

	private final SeekBar.OnSeekBarChangeListener mVolumeSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			updateVolume(seekBar.getProgress() / (float) seekBar.getMax());
		}
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {}
	};

	private final BroadcastPlayer.Observer mPlayerObserver = new BroadcastPlayer.Observer() {
		@Override
		public void onStateChange(PlayerState state) {
			boolean isPlayingLive = mBroadcastPlayer != null && mBroadcastPlayer.isTypeLive() && mBroadcastPlayer.isPlaying();
			TextView tvlive = ((Fragment_player)(adapter.getFragmentList().get(nowposition%3))).getLiveTextView();
//			TextView tvlive = mAryView.get(nowposition%3).findViewById(R.id.BroadcastLiveTextView);
			if (tvlive != null) {

				if(isPlayingLive) {
					mRl_Live.setVisibility(View.VISIBLE);
				}else {
					if(isChatGone)
						mRl_Live.setVisibility(View.GONE);

				}

				tvlive.setVisibility(isPlayingLive ? View.VISIBLE : View.GONE);
			}
//			updateLatencyView();
			Log.d(TAG,"state : " + state + "   nowposition : " + nowposition);
			if (state == PlayerState.PLAYING || state == PlayerState.PAUSED || state == PlayerState.COMPLETED) {
				if(state == PlayerState.PLAYING){
					setPreViewVisibility(nowposition,View.GONE);
				}else{
					setPreViewVisibility(nowposition,View.VISIBLE);
				}
//				if (mMediaController == null && mBroadcastPlayer != null && !mBroadcastPlayer.isTypeLive()) {
//					mMediaController = new MediaController(ExamplePlayerActivity.this);
//					mMediaController.setAnchorView(mVideoSurfaceView);
//					mMediaController.setMediaPlayer(mBroadcastPlayer);
//				}
//				if (mMediaController != null) {
//					mMediaController.setEnabled(true);
//					mMediaController.show();
//				}
			} else if (state == PlayerState.ERROR || state == PlayerState.CLOSED) {
				setPreViewVisibility(nowposition,View.VISIBLE);
//				if (mMediaController != null) {
//					mMediaController.setEnabled(false);
//					mMediaController.hide();
//				}
//				mMediaController = null;
			}
		}
		@Override
		public void onBroadcastLoaded(boolean live, int width, int height) {
			Log.d(TAG,"drdr onBroadcastLoaded");
			TextView tvlive = ((Fragment_player)(adapter.getFragmentList().get(nowposition%3))).getLiveTextView();
//			TextView tvlive = mAryView.get(nowposition%3).findViewById(R.id.BroadcastLiveTextView);
			if(live)
				mRl_Live.setVisibility(View.VISIBLE);
			else {
				if(isChatGone)
					mRl_Live.setVisibility(View.GONE);
			}

			if (tvlive != null)
				tvlive.setVisibility(live ? View.VISIBLE : View.GONE);
		}
	};

	private void updateLatencyView() {
//		if (mBroadcastLatencyTextView != null) {
//			LatencyMeasurement lm = mBroadcastPlayer != null ? mBroadcastPlayer.getEndToEndLatency() : null;
//			if (lm != null)
//				mBroadcastLatencyTextView.setText("Latency: " + (lm.latency / 1000.0) + " s");
//			mBroadcastLatencyTextView.setVisibility(lm != null ? View.VISIBLE : View.GONE);
//		}
	}

	private void setLatencyTimer(boolean enable) {
		mMainHandler.removeCallbacks(mLatencyUpdateRunnable);
		if (enable)
			mMainHandler.postDelayed(mLatencyUpdateRunnable, 1000);
	}

	private final Runnable mLatencyUpdateRunnable = new Runnable() {
		@Override
		public void run() {
			updateLatencyView();
			mMainHandler.postDelayed(this, 1000);
		}
	};

	private final BroadcastPlayer.ViewerCountObserver mViewerCountObserver = new BroadcastPlayer.ViewerCountObserver() {
		@Override
		public void onCurrentViewersUpdated(long viewers) {
			Log.d(TAG,"ViewerCountObserver " + viewers);
		}
		@Override
		public void onTotalViewersUpdated(long viewers) {
		}
	};

	@Override
	public void onDestroy() {
		Log.d(TAG,"connect : onDestroy " );
		if (mChannel != null) {
			ConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID);
			SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);

			ConnectionManager.logout(new SendBird.DisconnectHandler() {
				@Override
				public void onDisconnected() {
					try {
						PreferenceUtils.setConnected(false);
					}catch (Exception e){
						e.printStackTrace();
					}
				}
			});
//			mChannel.exit(new OpenChannel.OpenChannelExitHandler() {
//				@Override
//				public void onResult(SendBirdException e) {
//					Log.d(TAG,"connect : onDestroy " + 1);
//					if (e != null) {
//						// Error!
//						e.printStackTrace();
//						return;
//
//					}
//					Log.d(TAG,"connect : onDestroy " + 2);
//				}
//			});
		}

		super.onDestroy();
	}









	public void onCreateHeart(){
		// 로티 애니메이션뷰 리소스 아이디연결
		songLikeAnimButton = (LottieAnimationView)findViewById(R.id.button_song_like_animation);
		//Clicker01 = (LottieAnimationView)findViewById(R.id.clicker);

		// 텍스트뷰 리소스 아이디 연결
		isSongLikeAnimButtonClickedTextView = (TextView)findViewById(R.id.text_is_song_like_clicked);

		ClickIcon = (ImageView)findViewById(R.id.HeartIcon);


		// 애니메이션에 클릭 리스너를 붙인다.
		ClickIcon.setOnClickListener(new View.OnClickListener() {
			// 버튼이 클릭되었을 때
			@Override
			public void onClick(View v) {
				final String clapCountText = String.valueOf(++count);
				// 애니메이션을 발동시킨다.
				if(toggleSongLikeAnimButton()){
					// 좋아요 상태이면
					isSongLikeAnimButtonClickedTextView.setText(clapCountText);
				}
			}
		});
	}

	// 좋아요 로띠 애니메이션을 실행 시키는 메소드
	private boolean toggleSongLikeAnimButton(){
		// 애니메이션을 한번 실행시킨다.
		// Custom animation speed or duration.
		// ofFloat(시작 시간, 종료 시간).setDuration(지속시간)
		ValueAnimator animator = ValueAnimator.ofFloat(0f, 0.5f).setDuration(500);

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				songLikeAnimButton.setProgress((Float) animation.getAnimatedValue());
			}
		});
		animator.start();

		return true;

	}




	//신고 분류 팝업창
	public void select_Declare(View view) {
		final AlertDialog.Builder alert01 = new AlertDialog.Builder(ExamplePlayerActivity.this);
		View mView = getLayoutInflater().inflate(R.layout.declare_popup01, null);

		//신고항목
		final Button txt_input1 = (Button)mView.findViewById(R.id.txt_input1);
		final Button txt_input2 = (Button)mView.findViewById(R.id.txt_input2);
		final Button txt_input3 = (Button)mView.findViewById(R.id.txt_input3);
		final Button txt_input4 = (Button)mView.findViewById(R.id.txt_input4);
		final Button txt_input5 = (Button)mView.findViewById(R.id.txt_input5);
		final Button txt_input6 = (Button)mView.findViewById(R.id.txt_input6);

		//취소, 다음
		Button btn_cancel01 = (Button)mView.findViewById(R.id.btn_cancel1);
		Button btn_ok01 = (Button)mView.findViewById(R.id.btn_ok2);

		alert01.setView(mView);

		final AlertDialog alertDialog = alert01.create();
		alertDialog.setCanceledOnTouchOutside(false);

		txt_input1.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view) {
				select=0;
			}
		});
		txt_input1.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					txt_input1.setBackgroundColor(Color.TRANSPARENT);
				} else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
					txt_input1.setBackgroundColor(Color.MAGENTA);
				}
				return false;
			}
		});
		txt_input2.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view) {
				select=1;
			}
		});
		txt_input2.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					txt_input2.setBackgroundColor(Color.TRANSPARENT);
				} else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
					txt_input2.setBackgroundColor(Color.MAGENTA);
				}
				return false;
			}
		});
		txt_input3.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view) {
				select=2;
			}
		});
		txt_input3.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					txt_input3.setBackgroundColor(Color.TRANSPARENT);
				} else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
					txt_input3.setBackgroundColor(Color.MAGENTA);
				}
				return false;
			}
		});
		txt_input4.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view) {
				select=3;
			}
		});
		txt_input4.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					txt_input4.setBackgroundColor(Color.TRANSPARENT);
				} else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
					txt_input4.setBackgroundColor(Color.MAGENTA);
				}
				return false;
			}
		});
		txt_input5.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view) {
				select=4;
			}
		});
		txt_input5.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					txt_input5.setBackgroundColor(Color.TRANSPARENT);
				} else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
					txt_input5.setBackgroundColor(Color.MAGENTA);
				}
				return false;
			}
		});
		txt_input6.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view) {
				select=5;
			}
		});
		txt_input6.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					txt_input6.setBackgroundColor(Color.TRANSPARENT);
				} else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
					txt_input6.setBackgroundColor(Color.MAGENTA);
				}
				return false;
			}
		});






		btn_cancel01.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view) {
				alertDialog.dismiss();
			}
		});

//		btn_ok01.setOnClickListener(new View.OnClickListener(){
//			@Override
//			public void onClick(View view) {
//				alertDialog.dismiss();
//
//			}
//		});

		alertDialog.show();
	}


	//신고 사유 기술 팝업창
	public void write_Declare(View view) {
		final AlertDialog.Builder alert02 = new AlertDialog.Builder(ExamplePlayerActivity.this);
		View mView = getLayoutInflater().inflate(R.layout.declare_popup02, null);

		final EditText txt_input = (EditText)mView.findViewById(R.id.txt_input);
		Button btn_cancel02 = (Button)mView.findViewById(R.id.btn_cancel2);
		Button btn_ok02 = (Button)mView.findViewById(R.id.btn_ok2);

		alert02.setView(mView);

		final AlertDialog alertDialog = alert02.create();
		alertDialog.setCanceledOnTouchOutside(false);

		btn_cancel02.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View view) {
				alertDialog.dismiss();
			}
		});

		btn_ok02.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View view) {
				//txt_dummy_save에 신고 사유 기술 내용을 저장한다.
				txt_dummy_save = txt_input.getText().toString();
				alertDialog.dismiss();
			}
		});

		alertDialog.show();
	}


}