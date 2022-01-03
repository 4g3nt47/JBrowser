package com.umarabdul.jbrowser;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import java.net.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
* A headless browser using Jsoup.
*
* @author Umar Abdul
* @version 1.0
* @since 2020
*/

public class JBrowser{

  private Document pageDoc; // Document object of the current page.
  private String pageURL; // URL of current page.
  private String pageHTML; // HTML source of current page.
  private Connection.Response pageResponse; // Connection.Response object of the current page.
  private HashMap<String, ArrayList<String>> pageURLs; // URLs found in the current page.
  private ArrayList<String> history; // URLs visited by user in this session.
  private HashMap<String, String> requestHeaders; // HTTP headers to use in requests.
  private ArrayList<Element> pageForms; // HTTP forms in the current page.
  private Element selectedForm; // HTTP form selected.
  private HashMap<String, String> formParams; // Parameters of selected form.
  private boolean handleCookies; // Allow JBrowser to update requests with cookies received from server.
  private HashMap<String, String> pageCookies; // Cookies received for the current page.
  private HashMap<String, String> cookies; // HTTP cookies to use in requests.
  private boolean autoParse; // Automatically parse a pages when opened.
  private boolean parsed; // Indicates whether the current page was parsed using the JBrowser.parse() method.
  private boolean followRedirects; // Automatically resolve all HTTP redirects.
  private HashMap<String, String> proxy; // Proxy to use.
  private int timeout; // Read timeout, in milliseconds.
  private String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36"; // Default user agent.


  /**
  * JBrowser's constructor.
  */
  public JBrowser(){

    pageDoc = null;
    pageURL = null;
    pageHTML = null;
    pageResponse = null;
    pageURLs = new HashMap<String, ArrayList<String>>();
    pageURLs.put("href", new ArrayList<String>());
    pageURLs.put("img", new ArrayList<String>());
    pageURLs.put("js", new ArrayList<String>());
    pageURLs.put("css", new ArrayList<String>());
    history = new ArrayList<String>();
    requestHeaders = new HashMap<String, String>();
    pageForms = new ArrayList<Element>();
    selectedForm = null;
    formParams = new HashMap<String, String>();
    handleCookies = true;
    pageCookies = new HashMap<String, String>();
    cookies = new HashMap<String, String>();
    autoParse = true;
    parsed = false;
    followRedirects = true;
    proxy = new HashMap<String, String>();
    timeout = 10000;
    // set default user agent.
    setRequestHeader("User-Agent", userAgent);
  }

  /**
  * Control automatic parsing of loaded page to extract URLs and HTML forms.
  * @param flag {@code true/false}
  */
  public void setAutoParse(boolean flag){
    autoParse = flag;
  }

  /**
  * Enable/Disable resolution of redirects.
  * @param flag {@code true/false}.
  */
  public void setFollowRedirects(boolean flag){
    followRedirects = flag;
  }

  /**
  * Enable use of proxy.
  * @param host Proxy host.
  * @param port Proxy port.
  */
  public void setProxy(String host, int port){

    proxy.put("host", host);
    proxy.put("port", String.valueOf(port));
  }

  /**
  * Enable/Disable using cookies issued by pages.
  * @param flag {@code true/false}.
  */
  public void setHandleCookies(boolean flag){
    handleCookies = flag;
  }

  /**
  * Set read timeout.
  * @param timeout Timeout, in milliseconds.
  */
  public void setTimeout(int timeout){
    this.timeout = timeout;
  }

  /**
  * Get the default user agent header used by JBrowser.
  * @return User agent used by JBrowser.
  */
  public String getUserAgent(){
    return userAgent;
  }

  /**
  * Obtain a HashMap with keys href, css, js, and img, eached mapped to ArrayList containing URLs
  * of that type found in the current page. Only work after parsing with {@code parse()}.
  * @return A HashMap of absolute URLs found in the current page.
  */
  public HashMap<String, ArrayList<String>> getURLs(){
    return pageURLs;
  }

  /**
  * Obtain a list of all pages loaded using the browser instance.
  * @return ArrayList of URLs.
  */
  public ArrayList<String> getHistory(){
    return history;
  }

  /**
  * Set a header to include in all future requests.
  * For setting cookies, use {@link setCookie}.
  * @param key Name of header, e.g: User-Agent.
  * @param val Value to assign to the header.
  */
  public void setRequestHeader(String key, String val){
    requestHeaders.put(key, val);
  }

  /**
  * Set request headers from given HashMap.
  * @param headers HashMap of request headers.
  */
  public void setRequestHeader(HashMap<String, String> headers){

    for (String key : headers.keySet())
      requestHeaders.put(key, headers.get(key));
  }

  /**
  * Get the value in use for the given request header.
  * @param key Header name.
  * @return Value assigned to the header.
  */
  public String getRequestHeader(String key){
    return requestHeaders.get(key);
  }

  /**
  * Obtain a HashMap of all request headers in use.
  * @return A HashMap of all request headers.
  */
  public HashMap<String, String> getRequestHeaders(){
    return requestHeaders;
  }

  /**
  * Get the value of a response header for the current page.
  * @param key Header name.
  * @return Value assigned to the header.
  */
  public String getResponseHeader(String key){
    return pageResponse.header(key);
  }

  /**
  * Get the HTTP status code for the current page.
  * @return HTTP status code.
  */
  public int getStatusCode(){
    return pageResponse.statusCode();
  }

  /**
  * Fetch a form located at the given index in the current page.
  * @param index Index of HTML form.
  * @return Element instance of the selected form, {@code null} on index error.
  */
  public Element getForm(int index){
    
    try{
      return pageForms.get(index);
    }catch(IndexOutOfBoundsException e){
      return null;
    }
  }

  /**
  * Fetch a form using "id" attribute.
  * @param id ID of form to fetch.
  * @return Element instance of the selected form, {@code null} on index error.
  */
  public Element getForm(String id){

    for (Element elem : pageForms){
      if (elem.attr("id").equals(id))
        return elem;
    }
    return null;
  }

  /**
  * Obtain an ArrayList of all forms found in the current page.
  * @return An ArrayList of all forms found.
  */
  public ArrayList<Element> getForms(){
    return pageForms;
  }

  /**
  * Obtain the selected form in the current page.
  * @return Element instance of selected form.
  */
  public Element getSelectedForm(){
    return selectedForm;
  }

  /**
  * Select a form using the given index.
  * @param index Index of form to select.
  * @return {@code true} on success.
  */
  public boolean selectForm(int index){

    selectedForm = null;
    formParams.clear();
    // Select the form.
    try{
      selectedForm = pageForms.get(index);
    }catch(IndexOutOfBoundsException e){
      return false;
    }
    // Load parameters.
    Elements inputFields = getFormInputFields();
    String name = null;
    for (Element field : inputFields){
      name = field.attr("name");
      if (name.length() > 0)
        formParams.put(name, field.attr("value"));
    }
    return (selectedForm == null ? false : true);
  }

  /**
  * Select a form with the attribute "name" that have the value "value".
  * @param name Attribute name.
  * @param value Attribute value.
  * @return {@code true} on success.
  */
  public boolean selectFormByAttr(String name, String value){

    if (pageForms.size() == 0)
      return false;
    int index = 0;
    for (Element form : pageForms){
      if (form.attr(name).equals(value)){
        return selectForm(index);
      }
      index++;
    }
    return false;
  }

  /**
  * Obtain elements of all input fields for the selected form.
  * @return Elements instance of input fields.
  */
  public Elements getFormInputFields(){

    if (selectedForm == null)
      return null;
    return selectedForm.getElementsByTag("input");
  }

  /**
  * Assign a value to a parameter in the selected form.
  * @param name Name of the parameter.
  * @param value Value to assign to the parameter.
  */
  public void setFormParam(String name, String value){

    if (selectedForm != null)
      formParams.put(name, value);
  }

  /**
  * Obtain the value assigned to a parameter in the selected form.
  * @param name Name of the parameter.
  * @return Value of the parameter.
  */
  public String getFormParam(String name){
    return formParams.get(name);
  }

  /**
  * Obtain a HashMap of parameter names mapped to values for the selected form.
  * @return A HashMap of form parameters.
  */
  public HashMap<String, String> getFormParams(){
    return formParams;
  }

  /**
  * Obtain a HashMap of selected form attributes and their values.
  * @return A HashMap of form attributes.
  */
  public HashMap<String, String> getFormAttrs(){
    
    if (selectedForm == null)
      return null;
    HashMap<String, String> map = new HashMap<String, String>();
    Attributes attrs = selectedForm.attributes();
    for (Attribute attr : attrs)
      map.put(attr.getKey(), attr.getValue());
    return map;
  }

  /**
  * Test if all the parameters of the selected form have a value.
  * @return {@code true} on success.
  */
  public boolean formSatisfied(){

    if (selectedForm == null || formParams.size() == 0)
      return false;
    String val = null;
    for (String key : formParams.keySet()){
      val = formParams.get(key);
      if (val == null || val.length() == 0)
        return false;
    }
    return true;
  }

  /**
  * Submit the selected form.
  * @throws JBrowserException on error.
  */
  public void submitForm() throws JBrowserException{

    if (selectedForm == null)
      throw new JBrowserException("No form selected!");
    String url = selectedForm.absUrl("action");
    if (url.length() == 0)
      url = pageURL;
    String method = selectedForm.attr("method").toLowerCase();
    if (!(method.equals("post") || method.equals("get")))
      throw new JBrowserException("Invalid form submission method: " +method);
    if (method.equals("post"))
      open(url, Connection.Method.POST, formParams);
    else
      open(url, Connection.Method.GET, formParams);
  }

  /**
  * Set a request cookie using the given name and value.
  * @param name Cookie name.
  * @param value Cookie value.
  */
  public void setCookie(String name, String value){
    cookies.put(name, value);
  }

  /**
  * Set request cookies from given HashMap.
  * @param cookies HashMap of cookies, with names mapped to values.
  */
  public void setCookie(HashMap<String, String> cookies){

    for (String key : cookies.keySet())
      this.cookies.put(key, cookies.get(key));
  }

  /**
  * Fetch a request cookie with the given name.
  * @param name Name of the cookie.
  * @return Value of the cookie, null if not found.
  */
  public String getCookie(String name){
    return cookies.get(name);
  }

  /**
  * Obtain a HashMap of all request cookies in use.
  * @return Available cookies.
  */
  public HashMap<String, String> getCookies(){
    return cookies;
  }

  /**
  * Obtain a string representation of request cookies, in a format that can be used with "Cookie" HTTP header. 
  * @return A string of all available request cookies.
  */
  public String getCookiesString(){

    String str = "";
    String value = null;
    int i = 0;
    for (String key : cookies.keySet()){
      value = cookies.get(key);
      if (value == null || value.length() == 0)
        continue;
      if (i != cookies.size()-1)
        value += "; ";
      str += key + "=" + value;
      i++;
    }
    return str;
  }

  /**
  * Parse cookies string and return a HashMap of cookie names mapped to values.
  * @param cookieString Cookie string to parse.
  * @return A HashMap of cookie names and values.
  */
  public static HashMap<String, String> parseCookies(String cookieString){

    if (cookieString.startsWith("Cookie: ")){
      cookieString = cookieString.substring("Cookie: ".length());
    }else if (cookieString.startsWith("Set-Cookie: ")){
      cookieString = cookieString.substring("Set-Cookie: ".length());
    }
    String[] data = cookieString.split(";");
    String val = null;
    int pos;
    HashMap<String, String> parsed = new HashMap<String, String>();
    for (String s : data){
      s = s.trim();
      pos = s.indexOf("=");
      if (pos != -1)
        parsed.put(s.substring(0, pos), s.substring(pos+1));
    }
    return parsed;
  }

  /**
  * Parse header string, in {@code name=value,...} format.
  * @param header Header string to parse.
  * @return A HashMap of parsed header string.
  */
  public static HashMap<String, String> parseHeaders(String header){

    HashMap<String, String> map = new HashMap<String, String>();
    int pos = 0;
    for (String data : header.split(",")){
      data = data.trim();
      if (data.length() == 0)
        continue;
      pos = data.indexOf("=");
      if (pos != -1)
        map.put(data.substring(0, pos), data.substring(pos+1));
    }
    return map;
  }

  /**
  * Obtain HashMap of all cookies received from the current page.
  * @return Cookies received from the current page.
  */
  public HashMap<String, String> getPageCookies(){
    return pageCookies;
  }

  /**
  * Drop all cookies currently in use.
  */
  public void clearCookies(){
    cookies.clear();
  }

  /**
  * Obtain the URL of the current page.
  * @return URL of the current page.
  */
  public String getPageURL(){
    return pageURL;
  }

  /**
  * Obtain the content type for the current page.
  * @return Value of "Content-Type" header.
  */
  public String getPageContentType(){
    return pageResponse.contentType();
  }

  /**
  * Obtain the title of the current page.
  * @return Title of the current page.
  */
  public String getPageTitle(){
    return pageDoc.title();
  }

  /**
  * Fetch the HTML body of the current page.
  * @return HTML code of the current page.
  */
  public String getPageHTML(){
    return pageHTML;
  }

  /**
  * Obtain the Jsoup Document object of the current page.
  * @return Instance of Document for the current page.
  */
  public Document getPageDocument(){
    return pageDoc;
  }

  /**
  * Obtain the instance of Connection.Response for the current page.
  * @return Instance of Connection.Response.
  */
  public Connection.Response getPageResponse(){
    return pageResponse;
  }

  /**
  * Fetch elements that are in a given tag, having a given attribute with the given value.
  * @param tag Tag name.
  * @param name Name of attribute.
  * @param value Value of attribute.
  * @return ArrayList of Element.
  */
  public ArrayList<Element> getElementsByAttr(String tag, String name, String value){

    Elements elems = pageDoc.select(tag);
    ArrayList<Element> match = new ArrayList<Element>();
    for (Element elem : elems){
      if (elem.attr(name).equals(value))
        match.add(elem);
    }
    return match;
  }

  /**
  * Fully configure the browser with the currently loaded page. Should be called before using most of the methods.
  * It is called automatically when auto parsing is not disabled.
  * @throws JBrowserException on error.
  */
  public void parse() throws JBrowserException{

    if (pageHTML == null)
      throw new JBrowserException("No page available to parse!");
    if (parsed)
      return;
    // 1 - Extract URLs.
    pageURLs.get("href").clear();
    pageURLs.get("img").clear();
    pageURLs.get("css").clear();
    pageURLs.get("js").clear();
    Elements links = pageDoc.select("a[href]"); // Match href urls.
    ArrayList<String> holder = pageURLs.get("href");
    for (Element elem : links)
      holder.add(elem.absUrl("href"));
    links = pageDoc.select("img[src~=(?i)\\.(png|jpg|jpeg|gif)]"); // Match image URLs.
    holder = pageURLs.get("img");
    for (Element elem : links)
      holder.add(elem.absUrl("src"));
    links = pageDoc.select("script[src~=(?i)\\.js]"); // Match JavaScript links.
    holder = pageURLs.get("js");
    for (Element elem : links)
      holder.add(elem.absUrl("src"));
    links = pageDoc.select("link");
    holder = pageURLs.get("css");
    for (Element elem : links){
      if (elem.attr("type").equals("text/css"))
        holder.add(elem.absUrl("href"));
    }
    // Remove empty values added to list of urls when Element.absUrl() fails.
    for (String key : pageURLs.keySet()){
      holder = pageURLs.get(key);
      while (holder.contains(""))
        holder.remove("");
    }
    // 2 - Extract html forms.
    Elements forms = pageDoc.select("form");
    for (Element elem : forms)
      pageForms.add(elem);
    parsed = true;
  }

  /**
  * Open a URL and update the state of the browser.
  * @param url URL to load.
  * @param method HTTP request method to use.
  * @param data Data to use in the request.
  * @throws JBrowserException on error.
  */
  public void open(String url, Connection.Method method, HashMap<String, String> data) throws JBrowserException{

    parsed = false;
    Connection conn = null;
    pageResponse = null;
    try{
      conn = Jsoup.connect(url);
      if (proxy.size() >= 2){
        String host = proxy.get("host");
        String port = proxy.get("port");
        if (host != null && port != null)
          conn.proxy(host, Integer.parseInt(port));
      }
      if (requestHeaders.size() > 0)
        conn.headers(requestHeaders);
      if (cookies.size() > 0)
        conn.cookies(cookies);
      if (data != null)
        conn.data(data);
      pageResponse = conn.method(method).followRedirects(followRedirects).timeout(timeout).execute();
    }catch(Exception e){
      throw new JBrowserException("Error opening URL: " +e.getMessage());
    }
    try{
      pageDoc = pageResponse.parse();
    }catch(IOException e){
      throw new JBrowserException("Error parsing page: " +e.getMessage());
    }
    formParams.clear();
    selectedForm = null;
    pageForms.clear();
    pageURL = pageDoc.location();
    history.add(pageURL);
    pageHTML = pageDoc.body().toString();
    pageCookies = (HashMap<String, String>)(pageResponse.cookies());
    if (handleCookies){
      for (String key : pageCookies.keySet())
        setCookie(key, pageCookies.get(key));
    }
    if (autoParse)
      parse();
  }

  /**
  * Open a URL using GET request and update browser state.
  * @param url URL of page to open.
  * @throws JBrowserException on error.
  */
  public void open(String url) throws JBrowserException{
    open(url, Connection.Method.GET, null);
  }

  /**
  * Fetch the Document object of a page without updating browser state.
  * @param url URL of page.
  * @param method Request method.
  * @param data Data to use in the request.
  * @throws JBrowserException on error.
  * @return Instance of Document for the given URL.
  */
  public Document fetch(String url, Connection.Method method, HashMap<String, String> data) throws JBrowserException{

    Connection conn = null;
    Connection.Response rsp = null;
    try{
      conn = Jsoup.connect(url);
      if (proxy.size() >= 2){
        String host = proxy.get("host");
        String port = proxy.get("port");
        if (host != null && port != null)
          conn.proxy(host, Integer.parseInt(port));
      }
      if (requestHeaders.size() > 0)
        conn.headers(requestHeaders);
      if (cookies.size() > 0)
        conn.cookies(cookies);
      if (data != null)
        conn.data(data);
      rsp = conn.method(method).followRedirects(followRedirects).timeout(timeout).execute();
    }catch(Exception e){
      throw new JBrowserException("Error opening URL: " +e.getMessage());
    }
    try{
      return rsp.parse();
    }catch(IOException e){
      throw new JBrowserException("Error parsing page: " +e.getMessage());
    }
  }

  /**
  * Download the contents of a URL and save to disk, without updating browser state. This method does not use the Jsoup API.
  * @param url URL of page.
  * @param data Data to use in the request. If not null, POST request will be made, else, GET.
  * @param outfile Name of file to save contents to.
  * @throws JBrowserException on error.
  */
  public void download(String url, HashMap<String, String> data, String outfile) throws JBrowserException{

    DataOutputStream connDOS = null;
    DataInputStream connDIS = null;
    DataOutputStream fileDOS = null;
    try{
      URL urlObj = new URL(url);
      HttpURLConnection conn = null;
      if (proxy.size() >= 2)
        conn = (HttpURLConnection)urlObj.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.get("host"), Integer.parseInt(proxy.get("port")))));
      else
        conn = (HttpURLConnection)urlObj.openConnection();
      conn.setInstanceFollowRedirects(followRedirects);
      conn.setReadTimeout(timeout);
      conn.setRequestMethod(data != null ? "POST" : "GET");
      for (String key : requestHeaders.keySet())
        conn.setRequestProperty(key, requestHeaders.get(key));
      if (cookies.size() > 0)
        conn.setRequestProperty("Cookie", getCookiesString());
      byte[] postBuffer = null;
      if (data != null){ // parse POST data.
        String postData = "";
        int i = 0;
        for (String key : data.keySet()){
          if (i != data.size()-1)
            postData += key + "=" + data.get(key) + "&";
          else
            postData += key + "=" + data.get(key);
          i++;
        }
        postBuffer = postData.getBytes(StandardCharsets.UTF_8);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postBuffer.length));
        conn.setRequestProperty("charset", "utf-8");
        connDOS = new DataOutputStream(conn.getOutputStream());
        connDOS.write(postBuffer);
      }
      fileDOS = new DataOutputStream(new FileOutputStream(outfile));
      connDIS = new DataInputStream(conn.getInputStream());
      if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
        throw new IOException("Non 200 HTTP response code obtained!");
      byte[] buffer = new byte[9999];
      int len = 0;
      while ((len = connDIS.read(buffer)) != -1)
        fileDOS.write(buffer, 0, len);
      connDIS.close();
      if (connDOS != null)
        connDOS.close();
      fileDOS.close();
    }catch(Exception e){
      try{
        connDOS.close();
        connDIS.close();
        fileDOS.close();
      }catch(Exception e2){}
      throw new JBrowserException("Download error: " +e.getMessage());
    }
  }

}
