import java.io.*;
import java.net.*;
import org.json.*;

public class YoutubeComment {

	private int count;             // リプライではない通常コメントの数
	private int total_count;      // コメント総数
	private String key;	  // APIキー
	private String videoID;  // video_id
	private String next_url;
	private String base_url;


	YoutubeComment(String api_key, String video_ID) {
		this.count = 1;
		this.total_count = 0;
		this.key = api_key;
		this.videoID = video_ID;
		this.base_url = "https://www.googleapis.com/youtube/v3/commentThreads?part=snippet,replies&textFormat=plainText&maxResults=100&order=relevance" + "&videoId=" + this.videoID + "&key=" + this.key;
		this.next_url = this.base_url;
	}

	public void start() {
		start("sample.csv");
	}

	public void start(String file_name) {
		Boolean flag = true;
		String json_str; // JSONの文字列

		while(flag) {
			json_str = getJsonString();
			flag = jsonParsing(json_str, this.base_url, file_name);
		}

		System.out.println("END");
	}


	// URL先のJSONを文字列として返す
	private String getJsonString() {
		InputStream is = null;
		String line;
		String json_str = "";

		try {
			URL urlObj = new URL(this.next_url);
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
	private Boolean jsonParsing(String json_str, String base_url, String file_name) {
		//String br = System.getProperty("line.separator"); // 改行文字
		String channel_url; // コメント者のチャンネルURL
		String image_url; // サムネイルの画像URL
		String userName; // ユーザー名
		String date;     // 日付
		int likeCount;   // グッド数
		String comment;  // コメント内容
		String replie;   // リプライコメント
		String row_comment = "";
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
			row_comment = (this.total_count + 1) + "," + "Comment " + this.count++ + ",\"" + channel_url + "\",\"" + image_url + "\",\"" + userName + "\"," + date + "," + likeCount  + ",\"" + comment + "\""; // カンマが入る可能性のある文字列はダブルクォーテションで囲む
			fileWrite(row_comment, file_name);
			this.total_count++;

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
						row_comment = (this.total_count + 1) + "," + "Replie " + k + ",\"" + channel_url + "\",\"" + image_url + "\",\"" + userName +"\"," + date + "," + likeCount + "," + "\"" + replie + "\"";
						fileWrite(row_comment, file_name);
						this.total_count++;
					}
			}
		}

		// nextPagetokeの有無判定
		if (jObject.has("nextPageToken")) {
			this.next_url = this.base_url + "&pageToken=" + jObject.getString("nextPageToken");
			return true;
		}
		else {
			return false;
		}
	}

	// ファイルへ書き込む
	private void fileWrite(String row_comment, String file_name) {
		 PrintWriter pw = null;
        //FileWriterオブジェクトを生成する際にIOExceptionがスローされる可能性がある
        try {
            //PrintWriterクラスでラップしてコマンドライン引数でファイルを指定
            pw = new PrintWriter(new BufferedWriter(new FileWriter(file_name, true)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        pw.println(row_comment);      //print()メソッドで出力
        pw.close();
	}
}
