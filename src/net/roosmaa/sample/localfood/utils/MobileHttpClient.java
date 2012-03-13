package net.roosmaa.sample.localfood.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.os.Build;
import android.os.Looper;

public class MobileHttpClient extends DefaultHttpClient
{
  private static final int SOCKET_OPERATION_TIMEOUT = 60 * 1000;
  
  private static final HttpRequestInterceptor sThreadCheckInterceptor = new HttpRequestInterceptor()
  {
    @Override
    public void process(HttpRequest request, HttpContext context)
    {
      if (Looper.myLooper() != null
          && Looper.myLooper() == Looper.getMainLooper())
        throw new RuntimeException("HTTP requests forbidden on UI thread.");
    }
  };
  
  public static MobileHttpClient newInstance(String userAgent, Context context)
  {
    HttpParams params = new BasicHttpParams();
    
    HttpConnectionParams.setStaleCheckingEnabled(params, false);
    HttpConnectionParams.setSoTimeout(params, SOCKET_OPERATION_TIMEOUT);
    HttpConnectionParams.setSocketBufferSize(params, 8192);
    HttpProtocolParams.setUserAgent(params, userAgent);
    
    // If on a newer platform, use the Android's SSLCertificateSocketFactory:
    SSLSocketFactory sslSocketFactory = null;
    if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.FROYO */8)
    {
      try
      {
        Class<?> sessionCacheClass = Class
            .forName("android.net.SSLSessionCache");
        Constructor<?> sessionCacheCtor = sessionCacheClass
            .getConstructor(Context.class);
        Object sessionCache = sessionCacheCtor.newInstance(context);
        
        Class<?> socketFactoryClass = Class
            .forName("android.net.SSLCertificateSocketFactory");
        Method socketFactoryMethod = socketFactoryClass.getMethod(
            "getHttpSocketFactory", int.class, sessionCacheClass);
        sslSocketFactory = (SSLSocketFactory) socketFactoryMethod.invoke(null,
            SOCKET_OPERATION_TIMEOUT, sessionCache);
      }
      catch (ClassNotFoundException e)
      {
      }
      catch (SecurityException e)
      {
      }
      catch (NoSuchMethodException e)
      {
      }
      catch (IllegalArgumentException e)
      {
      }
      catch (InstantiationException e)
      {
      }
      catch (IllegalAccessException e)
      {
      }
      catch (InvocationTargetException e)
      {
      }
    }
    // Or fall back to Apache one:
    if (sslSocketFactory == null)
      sslSocketFactory = SSLSocketFactory.getSocketFactory();
    
    SchemeRegistry schemeRegistry = new SchemeRegistry();
    schemeRegistry.register(new Scheme("http", PlainSocketFactory
        .getSocketFactory(), 80));
    schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
    
    ClientConnectionManager manager = new ThreadSafeClientConnManager(params,
        schemeRegistry);
    
    return new MobileHttpClient(manager, params);
  }
  
  private MobileHttpClient(ClientConnectionManager conman, HttpParams params)
  {
    super(conman, params);
  }
  
  @Override
  protected BasicHttpProcessor createHttpProcessor()
  {
    BasicHttpProcessor proc = super.createHttpProcessor();
    proc.addRequestInterceptor(sThreadCheckInterceptor);
    return proc;
  }
  
  @Override
  protected void finalize() throws Throwable
  {
    close();
    super.finalize();
  }
  
  public void close()
  {
    getConnectionManager().shutdown();
  }
}
