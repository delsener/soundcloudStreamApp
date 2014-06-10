package ch.zhaw.seminar.soundcloud.stream.app.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.blinkenlights.jid3.ID3Exception;
import org.blinkenlights.jid3.MP3File;
import org.blinkenlights.jid3.v2.APICID3V2Frame;
import org.blinkenlights.jid3.v2.APICID3V2Frame.PictureType;
import org.blinkenlights.jid3.v2.ID3V2Tag;
import org.blinkenlights.jid3.v2.ID3V2_3_0Tag;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import ch.zhaw.seminar.soundcloud.stream.app.models.TrackListItem;
import ch.zhaw.seminar.soundcloud.stream.app.models.TrackListType;
import ch.zhaw.seminar.soundcloud.stream.app.ui.activities.MainNavigationActivity;

import com.soundcloud.api.Http;

public class SoundcloudStreamAppUtil {

	/** Default host URL. */
	public static final String SERVICE_URL = "http://10.0.0.4:8002";
	
	private static final String CLIENT_ID = "1658db91709a31648277029337106a74";

	private static final String TRACK_PLAYLIST_NAME = "SCStreamApp#Tracks";
	private static final String SET_PLAYLIST_NAME = "SCStreamApp#Sets";

	private static final String DOWNLOAD_DIRECTORY = "SoundcloudStreamApp";

	public static String fetchFavoritesJSON(String host, String user) {
		return fetchResource(host, "/stream/favorites/" + user);
	}
	
	public static String fetchExtendedStreamJSON(String host, String user) {
		return fetchResource(host, "/stream/news/" + user);
	}
	
	private static String fetchResource(String host, String res) {

		HttpClient httpclient = new DefaultHttpClient();
		try {
			HttpGet httpget = new HttpGet(host + res);

			System.out.println("executing request " + httpget.getURI());

			// Create a response handler
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpget, responseHandler);
			System.out.println("----------------------------------------");
			System.out.println(responseBody);
			System.out.println("----------------------------------------");

			String json = Http.formatJSON(responseBody);
			return json;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return StringUtils.EMPTY;
		} catch (IOException e) {
			e.printStackTrace();
			return StringUtils.EMPTY;
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}
	}

	public static void parseJsonTrackList(LinkedList<JSONObject> targets,
			String jsonTracks) throws JSONException {
		JSONArray tracks = new JSONArray(jsonTracks);

		for (int i = 0; i < tracks.length(); i++) {
			JSONObject track = tracks.getJSONObject(i);
			targets.add(track);
		}
	}

	public static MP3File writeMP3File(TrackListItem trackListItem) {
		if (StringUtils.isEmpty(trackListItem.getStreamURL())) {
			return null;
		}

		File file = createNewFile("Music", "soundcloudExt",
				trackListItem.getId() + ".mp3");

		try {
			// download file
			String streamUrl = trackListItem.getStreamURL() + "?client_id="
					+ CLIENT_ID;
			URLConnection conn = new URL(streamUrl).openConnection();
			InputStream is = conn.getInputStream();
			OutputStream outstream = new FileOutputStream(file);
			byte[] buffer = new byte[4096];
			int len;
			while ((len = is.read(buffer)) > 0) {
				outstream.write(buffer, 0, len);
			}
			outstream.flush();
			outstream.close();

			// set mp3 tags
			MP3File mp3 = new MP3File(file);

			ID3V2_3_0Tag oID3V2_3_0Tag = new ID3V2_3_0Tag();
			oID3V2_3_0Tag.setTitle(trackListItem.getTitle());
			oID3V2_3_0Tag.setArtist(trackListItem.getArtist());
			oID3V2_3_0Tag.setTrackNumber(trackListItem.getId());

			Drawable artwork = trackListItem.getArtwork();
			CompressFormat format = Bitmap.CompressFormat.JPEG;
			if (trackListItem.getArtworkURL() != null) {
				try {
					String artworkURL = StringUtils.replace(
							trackListItem.getArtworkURL(), "large", "original");
					System.out.println("downloading large image (jpg) "
							+ artworkURL);
					InputStream input = null;
					try {
						HttpURLConnection connection = (HttpURLConnection) new URL(
								artworkURL).openConnection();
						connection.connect();
						input = connection.getInputStream();
					} catch (FileNotFoundException ex) {
						artworkURL = StringUtils.replace(artworkURL, "jpg",
								"png");
						artworkURL = StringUtils.replace(artworkURL, "jpeg",
								"png");
						System.out.println("downloading large image (png) "
								+ artworkURL);
						HttpURLConnection connection = (HttpURLConnection) new URL(
								artworkURL).openConnection();
						connection.connect();
						input = connection.getInputStream();
						format = Bitmap.CompressFormat.PNG;
					}

					if (input != null) {
						Bitmap x = BitmapFactory.decodeStream(input);
						artwork = new BitmapDrawable(Resources.getSystem(), x);
					}
				} catch (Exception ex) {
					// ups, could not load artwork
					ex.printStackTrace();
				}
			}

			if (artwork != null) {
				Bitmap bitmap = ((BitmapDrawable) artwork).getBitmap();
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(format, 100, stream);
				byte[] bitmapdata = stream.toByteArray();
				String imageType = format == Bitmap.CompressFormat.PNG ? "image/png"
						: "image/jpeg";
				APICID3V2Frame image = new APICID3V2Frame(imageType,
						PictureType.FrontCover, String.valueOf(trackListItem
								.getId()), bitmapdata);
				oID3V2_3_0Tag.addAPICFrame(image);
			}

			boolean isSet = file.length() / 1024 > 10000;
			if (isSet) {
				oID3V2_3_0Tag.setGenre("Soundcloud Sets");
			} else {
				oID3V2_3_0Tag.setGenre("Soundcloud Tracks");
			}

			// set this v2.3.0 tag in the media file object
			mp3.setID3Tag(oID3V2_3_0Tag);
			// update the actual file to reflect the current state of our object
			mp3.sync();

			createAudioFile(file, mp3);

			if (trackListItem.getTrackListType() == TrackListType.TRACK) {
				addToPlaylist(TRACK_PLAYLIST_NAME, mp3);
			} else {
				addToPlaylist(SET_PLAYLIST_NAME, mp3);
			}

			return mp3;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static void deleteMP3File(TrackListItem trackListItem) {
		long audioFileId = findAudioFileId(trackListItem.getId());
		if (audioFileId == -1) {
			return;
		}

		File audioFile = findAudioFile(audioFileId);
		if (audioFile != null && audioFile.exists()) {
			audioFile.delete();
		}

		ContentResolver contentResolver = MainNavigationActivity.activity
				.getContentResolver();
		contentResolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				MediaStore.Audio.Media._ID + " LIKE \"" + audioFileId + "\"",
				null);

		long playlistId = findPlaylistId(TRACK_PLAYLIST_NAME);
		if (playlistId != -1) {
			Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
					"external", playlistId);
			contentResolver.delete(uri, MediaStore.Audio.Media._ID + " LIKE \""
					+ audioFileId + "\"", null);
		}

		playlistId = findPlaylistId(SET_PLAYLIST_NAME);
		if (playlistId != -1) {
			Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
					"external", playlistId);
			contentResolver.delete(uri, MediaStore.Audio.Media._ID + " LIKE \""
					+ audioFileId + "\"", null);
		}
	}

	public static void createPlaylist(String name) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(MediaStore.Audio.Playlists.NAME, name);
		contentValues.put(MediaStore.Audio.Playlists.DATE_ADDED,
				System.currentTimeMillis());
		contentValues.put(MediaStore.Audio.Playlists.DATE_MODIFIED,
				System.currentTimeMillis());
		ContentResolver contentResolver = MainNavigationActivity.activity
				.getContentResolver();
		contentResolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
				contentValues);
	}

	public static void createAudioFile(File file, MP3File mp3File) {
		try {
			ContentResolver contentResolver = MainNavigationActivity.activity
					.getContentResolver();

			ID3V2Tag tag = mp3File.getID3V2Tag();
			ContentValues contentValues = new ContentValues();
			contentValues.put(MediaStore.Audio.Media.DATA,
					file.getAbsolutePath());
			contentValues.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3");
			contentValues.put(MediaStore.Audio.Media.DATE_ADDED,
					System.currentTimeMillis());
			contentValues.put(MediaStore.Audio.Media.DATE_MODIFIED,
					System.currentTimeMillis());
			contentValues.put(MediaStore.Audio.Media.TITLE,
					"SCStreamApp# " + tag.getTitle());
			contentValues.put(MediaStore.Audio.Media.COMPOSER, tag.getArtist());
			contentValues.put(MediaStore.Audio.Media.TRACK,
					tag.getTrackNumber());
			contentValues.put(MediaStore.Audio.Media.ALBUM, tag.getTitle());
			contentValues.put(MediaStore.Audio.Media.IS_RINGTONE, true);
			contentValues.put(MediaStore.Audio.Media.IS_ALARM, true);
			contentValues.put(MediaStore.Audio.Media.IS_MUSIC, true);
			contentValues.put(MediaStore.Audio.Media.ARTIST, tag.getArtist());
			contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					contentValues);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addToPlaylist(String playlistName, MP3File mp3File) {
		long playlistId = findPlaylistId(playlistName);
		if (playlistId == -1) {
			createPlaylist(playlistName);
			playlistId = findPlaylistId(playlistName);
			if (playlistId == -1) {
				System.out.println(playlistName + " not found");
				return;
			}
		}

		try {
			long audioId = findAudioFileId(mp3File.getID3V2Tag()
					.getTrackNumber());
			if (audioId == -1) {
				return;
			}

			ContentResolver contentResolver = MainNavigationActivity.activity
					.getContentResolver();

			String[] cols = new String[] { "count(*)" };
			Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
					"external", playlistId);
			Cursor cur = contentResolver.query(uri, cols, null, null,
					MediaStore.Audio.Playlists.Members.PLAY_ORDER);
			cur.moveToFirst();
			int base = cur.getInt(0);
			System.out.println("base: " + base);
			cur.close();
			ContentValues values = new ContentValues();
			values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER,
					10000 - base);
			values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
			contentResolver.insert(uri, values);
		} catch (ID3Exception e) {
			e.printStackTrace();
		}

	}

	public static long findAudioFileId(int id) {
		ContentResolver contentResolver = MainNavigationActivity.activity
				.getContentResolver();

		Cursor cursor = contentResolver.query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media._ID },
				MediaStore.Audio.Media.TRACK + " LIKE \"" + id + "\"", null,
				null);
		if (cursor.getCount() == 0) {
			cursor.close();
			return -1;
		}

		cursor.moveToFirst();
		long audioFileId = cursor.getLong(0);
		cursor.close();
		return audioFileId;
	}

	public static File findAudioFile(long audioFileId) {
		ContentResolver contentResolver = MainNavigationActivity.activity
				.getContentResolver();

		Cursor cursor = contentResolver.query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media.DATA },
				MediaStore.Audio.Media._ID + " LIKE \"" + audioFileId + "\"",
				null, null);
		if (cursor.getCount() == 0) {
			cursor.close();
			return null;
		}

		cursor.moveToFirst();
		String path = cursor.getString(0);
		cursor.close();
		return new File(path);
	}

	public static long findPlaylistId(String playlistName) {
		ContentResolver contentResolver = MainNavigationActivity.activity
				.getContentResolver();

		Cursor cursor = contentResolver.query(
				MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Playlists._ID },
				MediaStore.Audio.Playlists.NAME + " LIKE \"" + playlistName
						+ "\"", null, null);
		if (cursor.getCount() == 0) {
			cursor.close();
			return -1;
		}

		cursor.moveToFirst();
		long playlistId = cursor.getLong(0);
		cursor.close();
		return playlistId;
	}

	public static List<TrackListItem> findDownloadedItems() {
		ContentResolver contentResolver = MainNavigationActivity.activity
				.getContentResolver();

		Cursor cursor = contentResolver.query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] {
						MediaStore.Audio.Media.TITLE,
						MediaStore.Audio.Media.COMPOSER,
						MediaStore.Audio.Media.TRACK },
				MediaStore.Audio.Media.TITLE + " LIKE \"SCStreamApp# %\"", null, null);
		if (cursor.getCount() == 0) {
			cursor.close();
			return new LinkedList<TrackListItem>();
		}

		List<TrackListItem> items = new LinkedList<TrackListItem>();
		while (cursor.moveToNext()) {
			TrackListItem item = new TrackListItem(cursor.getInt(2),
					TrackListType.DOWNLOADED_LOCAL, cursor.getString(0),
					cursor.getString(1), null);
			items.add(item);
		}
		cursor.close();
		return items;
	}

	public static File getFileFromSDCard(String filepath) {
		File rootDir = android.os.Environment.getExternalStorageDirectory();
		return new File(rootDir, DOWNLOAD_DIRECTORY + "/" + filepath);
	}

	public static File createNewFile(String rootDirectory, String subdirectory,
			String filename) {
		return createNewFile(null, rootDirectory, subdirectory, filename);
	}

	public static File createNewFile(InputStream inputStream,
			String rootDirectory, String subdirectory, String filename) {
		File rootDir = android.os.Environment.getExternalStorageDirectory();

		File fileDir = new File(rootDir.getAbsolutePath(),
				rootDirectory == null ? DOWNLOAD_DIRECTORY : rootDirectory);
		if (!fileDir.exists()) {
			fileDir.mkdir();
		}

		if (subdirectory != null) {
			fileDir = new File(fileDir, subdirectory);
			if (!fileDir.exists()) {
				fileDir.mkdirs();
			}
		}

		FileOutputStream outputStream = null;
		try {
			File file = new File(fileDir, filename);
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();

			if (inputStream == null) {
				return file;
			}

			outputStream = new FileOutputStream(file);
			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			return file;
		} catch (Exception e) {
			// DO SOMETHING HERE
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}
}
