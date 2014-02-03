package alipay.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtil {
	private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
	private static final String ENC = "UTF-8";
	
	private static CloseableHttpClient httpclient = HttpClients.createDefault();

	/**
	 * decode url query to map.
	 * @param urlEncodedString
	 * @return
	 */
	public static Map<String, String> urlDecode(String urlEncodedString) {
		try {
			String[] nvps = urlEncodedString.split("&");
			Map<String, String> pairs = new HashMap<String, String>();
			for (String nvp : nvps) {
				String[] pair = nvp.split("=");
				String name = URLDecoder.decode(pair[0], ENC);
				String value = null;
				if (pair.length > 1) {
					value = URLDecoder.decode(pair[1], ENC);
				}
				pairs.put(name, value);
			}
			return pairs;
		} catch (UnsupportedEncodingException e) {
			logger.error("unsupported encoding", e);
			throw new AssertionError();
		}
	}

	/**
	 * Encode map to url query string.
	 * @param params
	 * @return
	 */
	public static String urlEncode(Map<String, String> params) {
		StringBuilder sb = new StringBuilder();
		if (params != null) {
			for (Map.Entry<String, String> param : params.entrySet()) {
				if (sb.length() > 0) {
					sb.append('&');
				}
				try {
					sb.append(URLEncoder.encode(param.getKey(), ENC)).append(
							'=');

					String value = param.getValue();
					if (null == value) {
						value = "";
					}

					sb.append(URLEncoder.encode(value, ENC));
				} catch (UnsupportedEncodingException e) {
					logger.error("unsupported encoding", e);
					throw new AssertionError();
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Simple GET to server.
	 * @param urlvalue
	 * @return
	 */
    public static String doGet(String urlvalue) {
    	HttpGet httpget = new HttpGet(urlvalue);
    	return sendAndParseResponse(httpget);
    }

	/**
	 * Simple POST to server.
	 * @param urlvalue
	 * @return
	 */
    public static String doPost(String urlvalue) {
    	HttpPost httpget = new HttpPost(urlvalue);
    	return sendAndParseResponse(httpget);
    }
    
	private static String sendAndParseResponse(HttpUriRequest httpRequest) {
		try {
			return sendAndParseResponseImpl(httpRequest);
		} catch (ClientProtocolException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String sendAndParseResponseImpl(HttpUriRequest httpRequest)
			throws IOException, ClientProtocolException {
		CloseableHttpResponse response = httpclient.execute(httpRequest);
    	try {
    		HttpEntity entity = response.getEntity();
    		InputStream content = entity.getContent();
    		StringWriter writer = new StringWriter();
    		IOUtils.copy(content, writer, "utf-8");
    		String body = writer.toString();
    		return body;
    	} finally {
    		response.close();
    	}
	}
}
