import java.io.*;
import java.net.*;
import org.json.*;

public class Main {

	static int count = 1;             // リプライではない通常コメントの数
	static int total_count = 0;      // コメント総数
	static String API_URL;            // 主となるURL
	static String all_comments = ""; // 総コメント文

	public static void main(String[] args) {
		String key = "your_API";	  // APIキー
		String videoID = "video_id";  // video_id
		String base_url= "https://www.googleapis.com/youtube/v3/commentThreads?part=snippet,replies&textFormat=plainText&maxResults=100&order=relevance" + "&videoId=" + videoID + "&key=" + key;
		API_URL = base_url;
		Boolean flag = true;
		String json_str; // JSONの文字列

		while(flag) {
			json_str = getJsonString();
			flag = jsonParsing(json_str, base_url);
		}

		fileWrite();
		System.out.println("終了");
	}

	// URL先のJSONを文字列として返す
	public static String getJsonString() {
		InputStream is = null;
		String line;
		String json_str = "";

		try {
			URL urlObj = new URL(API_URL);
			is = urlObj.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			while ((line = br.readLine()) != null) {
				json_str += line;
			}

		} catch (MalformedURLException mue) {
			mue.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();

		} finally {
			try {
				if (is != null) is.close();
			} catch (IOException ioe) {

			}
		}
		return json_str;
	}

	// JSON文字列からJSONオブジェクトに変換し解析する
	public static Boolean jsonParsing(String json_str, String base_url) {
		String br = System.getProperty("line.separator"); // 改行文字
		String channel_url; // コメント者のチャンネルURL
		String image_url; // サムネイルの画像URL
		String userName; // ユーザー名
		String date;     // 日付
		int likeCount;   // グッド数
		String comment;  // コメント内容
		String replie;   // リプライコメント
		JSONObject jObject = new JSONObject(json_str);  // JSON文字列からJSONオブジェクトを生成
		JSONArray jArray = jObject.getJSONArray("items");
		JSONArray repliesArray;

		// コメント取得
		for (int i = 0; i < jArray.length(); i++) {
			channel_url = jArray.getJSONObject(i).getJSONObject("snippet").getJSONObject("topLevelComment").getJSONObject("snippet").getString("authorChannelUrl");
			image_url = jArray.getJSONObject(i).getJSONObject("snippet").getJSONObject("topLevelComment").getJSONObject("snippet").getString("authorProfileImageUrl");
			userName = jArray.getJSONObject(i).getJSONObject("snippet").getJSONObject("topLevelComment").getJSONObject("snippet").getString("authorDisplayName");
			date = jArray.getJSONObject(i).getJSONObject("snippet").getJSONObject("topLevelComment").getJSONObject("snippet").getString("publishedAt");
			comment = jArray.getJSONObject(i).getJSONObject("snippet").getJSONObject("topLevelComment").getJSONObject("snippet").getString("textDisplay").replace("\n"," ").replace("\r\n"," ").replace("\r"," "); // 改行を空白に置き換える
			likeCount = jArray.getJSONObject(i).getJSONObject("snippet").getJSONObject("topLevelComment").getJSONObject("snippet").getInt("likeCount");
			all_comments += (total_count + 1) + "," + "Comment " + count++ + ",\"" + channel_url + "\",\"" + image_url + "\",\"" + userName + "\"," + date + "," + likeCount  + ",\"" + comment + "\"" + br; // カンマが入る可能性のある文字列はダブルクォーテションで囲む
			total_count++;

			 // リプライコメントの有無判定
			if(jArray.getJSONObject(i).has("replies")) {
				repliesArray = jArray.getJSONObject(i).getJSONObject("replies").getJSONArray("comments");

					// リプライコメント取得 (古い順)
					for (int j = repliesArray.length() - 1, k = 1; 0 <= j ; j--, k++) {
						channel_url = repliesArray.getJSONObject(j).getJSONObject("snippet").getString("authorChannelUrl");
						image_url = repliesArray.getJSONObject(j).getJSONObject("snippet").getString("authorProfileImageUrl");
						userName = repliesArray.getJSONObject(j).getJSONObject("snippet").getString("authorDisplayName");
						date = jArray.getJSONObject(i).getJSONObject("snippet").getJSONObject("topLevelComment").getJSONObject("snippet").getString("publishedAt");
						replie = repliesArray.getJSONObject(j).getJSONObject("snippet").getString("textDisplay").replace("\n", " ").replace("\r\n"," ").replace("\r"," ");
						likeCount = repliesArray.getJSONObject(j).getJSONObject("snippet").getInt("likeCount");
						all_comments += (total_count + 1) + "," + "Replie " + k + ",\"" + channel_url + "\",\"" + image_url + "\",\"" + userName +"\"," + date + "," + likeCount + "," + "\"" + replie + "\"" + br;
						total_count++;
					}
			}
		}

		// nextPagetokeの有無判定
		if (jObject.has("nextPageToken")) {
			API_URL = base_url + "&pageToken=" + jObject.getString("nextPageToken");
			return true;
		}
		else {
			return false;
		}
	}

	// ファイルへ書き込む
	public static void fileWrite() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("test.csv"))) ;
			bw.write(all_comments);
			bw.close();

		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
