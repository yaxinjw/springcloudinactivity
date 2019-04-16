package com.example.eurekaclient.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.MimeHeaders;
import org.springframework.stereotype.Component;

/**
 * 解决git自动刷新报400的问题，具体参见https://blog.csdn.net/qq_42684642/article/details/86530761
 */
@Component
public class UrlFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;

		String url = new String(httpServletRequest.getRequestURI());

		//只过滤/actuator/refresh请求
		if (!url.endsWith("/refresh")) {
			chain.doFilter(request, response);
			return;
		}

//		// 我的搞法,但行不通
//		reflectSetparam(httpServletRequest, "accept-encoding", "gzip,deflate,br");
//		reflectSetparam(httpServletRequest, "accept-language", "zh-CN,zh;q=0.9");
//		chain.doFilter(httpServletRequest, response);

		//获取原始的body
		String body = readAsChars(httpServletRequest);

		System.out.println("original body:   " + body);

		//使用HttpServletRequest包装原始请求达到修改post请求中body内容的目的
		CustometRequestWrapper requestWrapper = new CustometRequestWrapper(httpServletRequest);

		chain.doFilter(requestWrapper, response);

	}

	/**
	 * 修改header信息，key-value键值对儿加入到header中
	 * @param request
	 * @param key
	 * @param value
	 */
	private static void reflectSetparam(HttpServletRequest request,String key,String value){
		Class<? extends HttpServletRequest> requestClass = request.getClass();
		System.out.println("request实现类="+requestClass.getName());
		try {
			Field request1 = requestClass.getDeclaredField("request");
			request1.setAccessible(true);
			Object o = request1.get(request);
			Field coyoteRequest = o.getClass().getDeclaredField("coyoteRequest");
			coyoteRequest.setAccessible(true);
			Object o1 = coyoteRequest.get(o);
			System.out.println("coyoteRequest实现类="+o1.getClass().getName());
			Field headers = o1.getClass().getDeclaredField("headers");
			headers.setAccessible(true);
			MimeHeaders o2 = (MimeHeaders)headers.get(o1);
			o2.addValue(key).setString(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {

	}

	private class CustometRequestWrapper extends HttpServletRequestWrapper {

		public CustometRequestWrapper(HttpServletRequest request) {
			super(request);
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			byte[] bytes = new byte[0];
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

			return new ServletInputStream() {
				@Override
				public boolean isFinished() {
					return byteArrayInputStream.read() == -1 ? true : false;
				}

				@Override
				public boolean isReady() {
					return false;
				}

				@Override
				public void setReadListener(ReadListener readListener) {

				}

				@Override
				public int read() throws IOException {
					return byteArrayInputStream.read();
				}
			};
		}
	}

	public static String readAsChars(HttpServletRequest request) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder("");
		try {
			br = request.getReader();
			String str;
			while ((str = br.readLine()) != null) {
				sb.append(str);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != br) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
}