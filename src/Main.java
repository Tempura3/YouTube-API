
public class Main {

	public static void main(String[] args) {
		YoutubeComment test = new YoutubeComment("API_KEY", "video_id");
		test.start(); // 引数にファイル名（なしの場合はsample.csvに書き込まれる）
	}

}
