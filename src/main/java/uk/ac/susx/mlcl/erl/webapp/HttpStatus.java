/*
 * Copyright (c) 2012, Hamish Morgan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of the University of Sussex nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.susx.mlcl.erl.webapp;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Constants enumerating the HTTP status codes.
 * <p/>
 * Yes another one! All the other libraries forgot the indispensable {@link #Im_a_teapot}.
 * <p/>
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Nonnull
@Nonnegative
public enum HttpStatus {

    /**
     * 0 NullStatus.
     * <p/>
     * A special status value that isn't actually defined in any specification. It's used to
     * indicate the absence of a status, rather than use Javas
     * <code>null</code> reference.
     */
    NullStatus(0, "Null"),
    /*
     * ***************************************************************************************
     *
     * 1xx Informational
     *
     * Request received, continuing process.[2] <p/> This class of status code indicates a
     * provisional response, consisting only of the Status-Line and optional headers, and is
     * terminated by an empty line. Since HTTP/1.0 did not define any 1xx status statuses, servers
     * must not send a 1xx response to an HTTP/1.0 client except under experimental conditions.
     *
     * **************************************************************************************
     */
    /**
     * 100 Continue
     * <p/>
     * This means that the server has received the request headers, and that the client should
     * proceed to send the request body (in the case of a request for which a body needs to be sent;
     * for example, a POST request). If the request body is large, sending it to a server when a
     * request has already been rejected based upon inappropriate headers is inefficient. To have a
     * server check if the request could be accepted based on the request's headers alone, a client
     * must send Expect: 100-continue as a header in its initial request[2] and check if a 100
     * Continue status code is received in response before continuing (or receive 417 Expectation
     * Failed and not continue).[2]
     * <p/>
     */
    Continue(100, "Continue"),
    /**
     * 101 Switching Protocols.
     * <p/>
     * This means the requester has asked the server to switch protocols and the server is
     * acknowledging that it will do so.[2]
     */
    Switching_Protocols(101, "Switching Protocols"),
    /**
     * 202 Processing (WebDAV; RFC 2518).
     * <p/>
     * As a WebDAV request may contain many sub-requests involving file operations, it may take a
     * long time to complete the request. This code indicates that the server has received and is
     * processing the request, but no response is available yet.[3] This prevents the client from
     * timing out and assuming the request was lost.
     */
    Processing(102, "Processing"),
    /*
     * ***************************************************************************************
     *
     * 2xx Success.
     *
     * This class of status statuses indicates the action requested by the client was received,
     * understood, accepted and processed successfully.
     *
     * ***************************************************************************************
     */
    /**
     * 200 OK.
     * <p/>
     * Standard response for successful HTTP requests. The actual response will depend on the
     * request method used. In a GET request, the response will contain an entity corresponding to
     * the requested resource. In a POST request the response will contain an entity describing or
     * containing the result of the action.[2]
     */
    OK(200, "OK"),
    /**
     * 201 Created.
     * <p/>
     * The request has been fulfilled and resulted in a new resource being created.[2]
     */
    Created(201, "Created"),
    /**
     * 202 Accepted.
     * <p/>
     * The request has been accepted for processing, but the processing has not been completed. The
     * request might or might not eventually be acted upon, as it might be disallowed when
     * processing actually takes place.[2]
     */
    Accepted(202, "Accepted"),
    /**
     * 203 Non-Authoritative Information (since HTTP/1.1).
     * <p/>
     * The server successfully processed the request, but is returning information that may be from
     * another source.[2]
     */
    NonAuthoritative_Information(203, "Non-Authoritative Information"),
    /**
     * 204 No Content.
     * <p/>
     * The server successfully processed the request, but is not returning any content.[2]
     */
    No_Content(204, "No Content"),
    /**
     * 205 Reset Content.
     * <p/>
     * The server successfully processed the request, but is not returning any content. Unlike a 204
     * response, this response requires that the requester reset the document view.[2]
     */
    Reset_Content(205, "Reset Content"),
    /**
     * 206 Partial Content.
     * <p/>
     * The server is delivering only part of the resource due to a range header sent by the client.
     * The range header is used by tools like wget to enable resuming of interrupted downloads, or
     * split a download into multiple simultaneous streams.[2]
     */
    Partial_Content(206, "Partial Content"),
    /**
     * 207 Multi-Status (WebDAV; RFC 4918).
     * <p/>
     * The message body that follows is an XML message and can contain a number of separate response
     * statuses, depending on how many sub-requests were made.[4]
     */
    Multi_Status(207, "Multi-Status"),
    /**
     * 208 Already Reported (WebDAV; RFC 5842).
     * <p/>
     * The members of a DAV binding have already been enumerated in a previous reply to this
     * request, and are not being included again.
     */
    Already_Reported(208, "Already Reported"),
    /**
     * 226 IM Used (RFC 3229).
     * <p/>
     * The server has fulfilled a GET request for the resource, and the response is a representation
     * of the result of one or more instance-manipulations applied to the current instance.[5]
     */
    IM_Used(226, "IM Used"),
    /*
     * ***************************************************************************************
     *
     * 3xx Redirection.
     *
     * The client must take additional action to complete the request.[2] This class of status code
     * indicates that further action needs to be taken by the user agent in order to fulfil the
     * request. The action required may be carried out by the user agent without interaction with
     * the user if and only if the method used in the second request is GET or HEAD. A user agent
     * should not automatically redirect a request more than five times, since such redirections
     * usually indicate an infinite loop.
     *
     * ***************************************************************************************
     */
    /**
     * 300 Multiple Choices.
     * <p/>
     * Indicates multiple options for the resource that the client may follow. It, for instance,
     * could be used to present different format options for video, list files with different
     * extensions, or word sense disambiguation.[2]
     */
    Multiple_Choices(300, "Multiple Choices"),
    /**
     * 301 Moved Permanently.
     * <p/>
     * This and all future requests should be directed to the given URI.[2]
     */
    Moved_Permanently(300, "Moved Permanently"),
    /**
     * 302 Found.
     * <p/>
     * This is an example of industry practice contradicting the standard.[2] The HTTP/1.0
     * specification (RFC 1945) required the client to perform a temporary redirect (the original
     * describing phrase was "Moved Temporarily"),[6] but popular browsers implemented 302 with the
     * functionality of a 303 See Other. Therefore, HTTP/1.1 added status statuses 303 and 307 to
     * distinguish between the two behaviours.[7] However, some Web applications and frameworks use
     * the 302 status code as if it were the 303.[8][citation needed]
     */
    Found(302, ""),
    Moved_Temporarily(302, "Found"),
    /**
     * 303 See Other (since HTTP/1.1).
     * <p/>
     * The response to the request can be found under another URI using a GET method. When received
     * in response to a POST (or PUT/DELETE), it should be assumed that the server has received the
     * data and the redirect should be issued with a separate GET message.[2]
     */
    See_Other(303, "See Other"),
    /**
     * 304 Not Modified.
     * <p/>
     * Indicates the resource has not been modified since last requested.[2] Typically, the HTTP
     * client provides a header like the If-Modified-Since header to provide a time against which to
     * compare. Using this saves bandwidth and reprocessing on both the server and client, as only
     * the header data must be sent and received in comparison to the entirety of the page being
     * re-processed by the server, then sent again using more bandwidth of the server and client.
     */
    Not_Modified(304, "Not Modified"),
    /**
     * 305 Use Proxy (since HTTP/1.1).
     * <p/>
     * Many HTTP clients (such as Mozilla[9] and Internet Explorer) do not correctly handle
     * responses with this status code, primarily for security reasons.[2][not in citation given]
     */
    Use_Proxy(305, ""),
    /**
     * 306 Switch Proxy.
     * <p/>
     * No longer used.[2] Originally meant "Subsequent requests should use the specified proxy."[10]
     */
    Switch_Proxy(306, "Switch Proxy"),
    /**
     * 307 Temporary Redirect (since HTTP/1.1).
     * <p/>
     * In this case, the request should be repeated with another URI; however, future requests
     * should still use the original URI.[2] In contrast to how 302 was historically implemented,
     * the request method is not allowed to be changed when reissuing the original request. For
     * instance, a POST request repeated using another POST request.[11]
     */
    Temporary_Redirect(307, "Temporary Redirect"),
    /**
     * 308 Permanent Redirect (approved as experimental RFC)[12].
     * <p/>
     * The request, and all future requests should be repeated using another URI. 307 and 308 (as
     * proposed) parallel the behaviours of 302 and 301, but do not allow the HTTP method to change.
     * So, for example, submitting a form to a permanently redirected resource may continue
     * smoothly.
     */
    Permanent_Redirect(308, "Permanent Redirect"),
    /*
     * ***************************************************************************************
     *
     * 4xx Client Error.
     *
     * The 4xx class of status code is intended for cases in which the client seems to have erred.
     * Except when responding to a HEAD request, the server should include an entity containing an
     * explanation of the error situation, and whether it is a temporary or permanent condition.
     * These status statuses are applicable to any request method. User agents should display any
     * included entity to the user.
     *
     * ***************************************************************************************
     */
    /**
     * 400 Bad Request.
     * <p/>
     * The request cannot be fulfilled due to bad syntax.[2]
     */
    Bad_Request(400, "Bad Request"),
    /**
     * 401 Unauthorized.
     * <p/>
     * Similar to 403 Forbidden, but specifically for use when authentication is required and has
     * failed or has not yet been provided.[2] The response must include a WWW-Authenticate header
     * field containing a challenge applicable to the requested resource. See Basic access
     * authentication and Digest access authentication.
     */
    Unauthorized(401, "Unauthorized"),
    /**
     * 402 Payment Required.
     * <p/>
     * Reserved for future use.[2] The original intention was that this code might be used as part
     * of some form of digital cash or micropayment scheme, but that has not happened, and this code
     * is not usually used. As an example of its use, however, Apple's MobileMe service generates a
     * 402 error if the MobileMe account is delinquent.[citation needed]
     */
    Payment_Required(402, "Payment Required"),
    /**
     * 403 Forbidden.
     * <p/>
     * The request was a valid request, but the server is refusing to respond to it.[2] Unlike a 401
     * Unauthorized response, authenticating will make no difference.[2] On servers where
     * authentication is required, this commonly means that the provided credentials were
     * successfully authenticated but that the credentials still do not grant the client permission
     * to access the resource (e.g. a recognized user attempting to access restricted content).
     */
    Forbidden(403, "Forbidden"),
    /**
     * 404 Not Found.
     * <p/>
     * The requested resource could not be found but may be available again in the future.[2]
     * Subsequent requests by the client are permissible.
     */
    Not_Found(404, "Not Found"),
    /**
     * 405 Method Not Allowed.
     * <p/>
     * A request was made of a resource using a request method not supported by that resource;[2]
     * for example, using GET on a form which requires data to be presented via POST, or using PUT
     * on a read-only resource.
     */
    Method_Not_Allowed(405, "Method Not Allowed"),
    /**
     * 406 Not Acceptable.
     * <p/>
     * The requested resource is only capable of generating content not acceptable according to the
     * Accept headers sent in the request.[2]
     */
    Not_Acceptable(406, "Not Acceptable"),
    /**
     * 407 Proxy Authentication Required.
     * <p/>
     * The client must first authenticate itself with the proxy.[2]
     */
    Proxy_Authentication_Required(407, "Proxy Authentication Required"),
    /**
     * 408 Request Timeout.
     * <p/>
     * The server timed out waiting for the request.[2] According to W3 HTTP specifications: "The
     * client did not produce a request within the time that the server was prepared to wait. The
     * client MAY repeat the request without modifications at any later time."
     */
    Request_Timeout(408, "Request Timeout"),
    /**
     * 409 Conflict.
     * <p/>
     * Indicates that the request could not be processed because of conflict in the request, such as
     * an edit conflict.[2]
     */
    Conflict(409, "Conflict"),
    /**
     * 410 Gone.
     * <p/>
     * Indicates that the resource requested is no longer available and will not be available
     * again.[2] This should be used when a resource has been intentionally removed and the resource
     * should be purged. Upon receiving a 410 status code, the client should not request the
     * resource again in the future. Clients such as search engines should remove the resource from
     * their indices. Most use cases do not require clients and search engines to purge the
     * resource, and a "404 Not Found" may be used instead.
     */
    Gone(410, "Gone"),
    /**
     * 411 Length Required.
     * <p/>
     * The request did not specify the length of its content, which is required by the requested
     * resource.[2]
     */
    Length_Required(411, "Length Required"),
    /**
     * 412 Precondition Failed.
     * <p/>
     * The server does not meet one of the preconditions that the requester put on the request.[2]
     */
    Precondition_Failed(412, "Precondition Failed"),
    /**
     * 413 Request Entity Too Large.
     * <p/>
     * The request is larger than the server is willing or able to process.[2]
     */
    Request_Entity_Too_Large(413, "Request Entity Too Large"),
    /**
     * 414 Request-URI Too Long.
     * <p/>
     * The URI provided was too long for the server to process.[2]
     */
    RequestURI_Too_Long(414, "Request-URI Too Long"),
    /**
     * 415 Unsupported Media Type.
     * <p/>
     * The request entity has a media type which the server or resource does not support.[2] For
     * example, the client uploads an image as image/svg+xml, but the server requires that images
     * use a different format.
     */
    Unsupported_Media_Type(415, "Unsupported Media Type"),
    /**
     * 416 Requested Range Not Satisfiable.
     * <p/>
     * The client has asked for a portion of the file, but the server cannot supply that portion.[2]
     * For example, if the client asked for a part of the file that lies beyond the end of the
     * file.[2]
     */
    Requested_Range_Not_Satisfiable(416, "Requested Range Not Satisfiable"),
    /**
     * 417 Expectation Failed.
     * <p/>
     * The server cannot meet the requirements of the Expect request-header field.[2]
     */
    Expectation_Failed(417, "Expectation Failed"),
    /**
     * 418 I'm a teapot (RFC 2324).
     * <p/>
     * This code was defined in 1998 as one of the traditional IETF April Fools' jokes, in RFC 2324,
     * Hyper Text Coffee Pot Control Protocol, and is not expected to be implemented by actual HTTP
     * servers
     */
    Im_a_teapot(418, "I'm a teapot"),
    /**
     * 420 Enhance Your Calm (Twitter).
     * <p/>
     * Not part of the HTTP standard, but returned by the Twitter Search and Trends API when the
     * client is being rate limited.[13] Other services may wish to implement the 429 Too Many
     * Requests response code instead.
     */
    Enhance_Your_Calm(420, "Enhance Your Calm"),
    /**
     * 422 Unprocessable Entity (WebDAV; RFC 4918).
     * <p/>
     * The request was well-formed but was unable to be followed due to semantic errors.[4]
     */
    Unprocessable_Entity(422, "Unprocessable Entity"),
    /**
     * 423 Locked (WebDAV; RFC 4918).
     * <p/>
     * The resource that is being accessed is locked.[4]
     */
    Locked(423, "Locked"),
    /**
     * 424 Failed Dependency (WebDAV; RFC 4918).
     * <p/>
     * The request failed due to failure of a previous request (e.g. a PROPPATCH).[4]
     */
    Failed_Dependency(424, "Failed Dependency"),
    /**
     * 424 Method Failure (WebDAV)[14].
     * <p/>
     * Indicates the method was not executed on a particular resource within its scope because some
     * part of the method's execution failed causing the entire method to be aborted.
     */
    Method_Failure(424, "Method Failure"),
    /**
     * 425 Unordered Collection (Internet draft).
     * <p/>
     * Defined in drafts of "WebDAV Advanced Collections Protocol",[15] but not present in "Web
     * Distributed Authoring and Versioning (WebDAV) Ordered Collections Protocol".[16]
     */
    Unordered_Collection(425, "Unordered Collection"),
    /**
     * 426 Upgrade Required (RFC 2817).
     * <p/>
     * The client should switch to a different protocol such as TLS/1.0.[17]
     */
    Upgrade_Required(426, "Upgrade Required"),
    /**
     * 428 Precondition Required (RFC 6585).
     * <p/>
     * The origin server requires the request to be conditional. Intended to prevent "the 'lost
     * update' problem, where a client GETs a resource's state, modifies it, and PUTs it back to the
     * server, when meanwhile a third party has modified the state on the server, leading to a
     * conflict."[18]
     */
    Precondition_Required(428, "Precondition Required"),
    /**
     * 429 Too Many Requests (RFC 6585).
     * <p/>
     * The user has sent too many requests in a given amount of time. Intended for use with rate
     * limiting schemes.[18]
     */
    Too_Many_Requests(429, "Too Many Requests"),
    /**
     * 431 Request Header Fields Too Large (RFC 6585).
     * <p/>
     * The server is unwilling to process the request because either an individual header field, or
     * all the header fields collectively, are too large.[18]
     */
    Request_Header_Fields_Too_Large(431, "Request Header Fields Too Large"),
    /**
     * 444 No Response (Nginx).
     * <p/>
     * Used in Nginx logs to indicate that the server has returned no information to the client and
     * closed the connection (useful as a deterrent for malware).
     */
    No_Response(444, "No Response"),
    /**
     * 449 Retry With (Microsoft).
     * <p/>
     * A Microsoft extension. The request should be retried after performing the appropriate
     * action.[19]
     * <p/>
     * Often search-engines or custom applications will ignore required parameters. Where no default
     * action is appropriate, the Aviongoo website sends a "HTTP/1.1 449 Retry with valid
     * parameters: param1, param2, . . ." response. The applications may choose to learn, or not.
     */
    Retry_With(449, "Retry With"),
    /**
     * 450 Blocked by Windows Parental Controls (Microsoft).
     * <p/>
     * A Microsoft extension. This error is given when Windows Parental Controls are turned on and
     * are blocking access to the given webpage.[20]
     */
    Blocked_by_Windows_Parental_Controls(450, "Blocked by Windows Parental Controls"),
    /**
     * 451 Unavailable For Legal Reasons (Internet draft).
     * <p/>
     * Defined in the internet draft "A New HTTP Status Code for Legally-restricted Resources".[21]
     * Intended to be used when resource access is denied for legal reasons, e.g. censorship or
     * government-mandated blocked access. A reference to the 1953 dystopian novel Fahrenheit 451,
     * where books are outlawed.[22]
     */
    Unavailable_For_Legal_Reasons(451, "Unavailable For Legal Reasons"),
    /**
     * 451 Redirect (Microsoft).
     * <p/>
     * Used in Exchange ActiveSync if there either is a more efficient server to use or the server
     * can't access the users' mailbox.[23]
     * <p/>
     * The client is supposed to re-run the HTTP Autodiscovery protocol to find a better suited
     * server.[24]
     */
    Redirect(451, "Redirect"),
    /**
     * 494 Request Header Too Large (Nginx).
     * <p/>
     * Nginx internal code similar to 431 but it was introduced earlier.[25]
     */
    Request_Header_Too_Large(494, "Request Header Too Large"),
    /**
     * 495 Cert Error (Nginx).
     * <p/>
     * Nginx internal code used when SSL client certificate error occurred to distinguish it from
     * 4XX in a log and an error page redirection.
     */
    Cert_Error(495, "Cert Error"),
    /**
     * 496 No Cert (Nginx).
     * <p/>
     * Nginx internal code used when client didn't provide certificate to distinguish it from 4XX in
     * a log and an error page redirection.
     */
    No_Cert(496, "No Cert"),
    /**
     * 497 HTTP to HTTPS (Nginx).
     * <p/>
     * Nginx internal code used for the plain HTTP requests that are sent to HTTPS port to
     * distinguish it from 4XX in a log and an error page redirection.
     */
    HTTP_to_HTTPS(497, "HTTP to HTTPS"),
    /**
     * 499 Client Closed Request (Nginx).
     * <p/>
     * Used in Nginx logs to indicate when the connection has been closed by client while the server
     * is still processing its request, making server unable to send a status code back.[26]
     */
    Client_Closed_Request(499, "Client Closed Request"),
    /*
     * ***************************************************************************************
     *
     * 5xx Server Error.
     *
     * The server failed to fulfill an apparently valid request.[2] Response status statuses
     * beginning with the digit "5" indicate cases in which the server is aware that it has
     * encountered an error or is otherwise incapable of performing the request. Except when
     * responding to a HEAD request, the server should include an entity containing an explanation
     * of the error situation, and indicate whether it is a temporary or permanent condition.
     * Likewise, user agents should display any included entity to the user. These response statuses
     * are applicable to any request method.
     *
     * ***************************************************************************************
     */
    /**
     * 500 Internal Server Error.
     * <p/>
     * A generic error message, given when no more specific message is suitable.[2]
     */
    Internal_Server_Error(500, "Internal Server Error"),
    /**
     * 501 Not Implemented.
     * <p/>
     * The server either does not recognize the request method, or it lacks the ability to fulfill
     * the request.[2]
     */
    Not_Implemented(501, "Not Implemented"),
    /**
     * 502 Bad Gateway.
     * <p/>
     * The server was acting as a gateway or proxy and received an invalid response from the
     * upstream server.[2]
     */
    Bad_Gateway(502, "Bad Gateway"),
    /**
     * 503 Service Unavailable.
     * <p/>
     * The server is currently unavailable (because it is overloaded or down for maintenance).[2]
     * Generally, this is a temporary state.
     */
    Service_Unavailable(503, "Service Unavailable"),
    /**
     * 504 Gateway Timeout.
     * <p/>
     * The server was acting as a gateway or proxy and did not receive a timely response from the
     * upstream server.[2]
     */
    Gateway_Timeout(504, "Gateway Timeout"),
    /**
     * 505 HTTP Version Not Supported.
     * <p/>
     * The server does not support the HTTP protocol version used in the request.[2]
     */
    HTTP_Version_Not_Supported(505, "HTTP Version Not Supported"),
    /**
     * 506 Variant Also Negotiates (RFC 2295).
     * <p/>
     * Transparent content negotiation for the request results in a circular reference.[27]
     */
    Variant_Also_Negotiates(506, "Variant Also Negotiates"),
    /**
     * 507 Insufficient Storage (WebDAV; RFC 4918).
     * <p/>
     * The server is unable to store the representation needed to complete the request.[4]
     */
    Insufficient_Storag(507, "Insufficient Storage"),
    /**
     * 508 Loop Detected (WebDAV; RFC 5842).
     * <p/>
     * The server detected an infinite loop while processing the request (sent in lieu of 208).
     */
    Loop_Detected(508, "Loop Detected"),
    /**
     * 509 Bandwidth Limit Exceeded (Apache bw/limited extension).
     * <p/>
     * This status code, while used by many servers, is not specified in any RFCs.
     */
    Bandwidth_Limit_Exceeded(509, "Bandwidth Limit Exceeded"),
    /**
     * 510 Not Extended (RFC 2774).
     * <p/>
     * Further extensions to the request are required for the server to fulfill it.[28]
     */
    Not_Extended(510, "Not Extended"),
    /**
     * 511 Network Authentication Required (RFC 6585).
     * <p/>
     * The client needs to authenticate to gain network access. Intended for use by intercepting
     * proxies used to control access to the network (e.g. "captive portals" used to require
     * agreement to Terms of Service before granting full Internet access via a Wi-Fi hotspot).[18]
     */
    Network_Authentication_Required(511, "Network Authentication Required"),
    /**
     * 598 Network read timeout error (Unknown).
     * <p/>
     * This status code is not specified in any RFCs, but is used by Microsoft Corp. HTTP proxies to
     * signal a network read timeout behind the proxy to a client in front of the proxy.
     */
    Network_read_timeout_error(598, "Network read timeout error"),
    /**
     * 599 Network connect timeout error (Unknown).
     * <p/>
     * This status code is not specified in any RFCs, but is used by Microsoft Corp. HTTP proxies to
     * signal a network connect timeout behind the proxy to a client in front of the proxy.
     */
    Network_connect_timeout_error(599, "Network connect timeout error");

    /*
     * ***************************************************************************************
     */
    /**
     * integer status code
     */
    private final int code;

    /**
     * human readable string description
     */
    private final String message;

//    /**
//     * class of this status
//     */
//    private Type statusClass;

    /**
     * Private constructor.
     * <p/>
     * @param code    integer code for this status
     * @param message human readable text message
     * @throws IllegalArgumentException of code is negative
     * @throws NullPointerException     if message is null
     */
    private HttpStatus(int code, String message) {
        Preconditions.checkArgument(
                code >= 0, "Expecting positive argument code, but found %s.", code);
        Preconditions.checkNotNull(message, "message");
        this.code = code;
        this.message = message;
    }

    /**
     * Get the integer code for this status.
     * <p/>
     * @return integer status code
     */
    public int code() {
        return code;
    }

    /**
     * Get the associated human readable message for this status
     * <p/>
     * @return human readable text message
     */
    public String message() {
        return message;
    }

    /**
     * Get whether or not this status is defined in the HTTP/1.0 specification.
     * <p/>
     * @return true if this status is defined in the HTTP/1.0 specification, false otherwise.
     */
    public boolean isHttpVersion10() {
        return Lazy.HTTP_1_0.contains(this);
    }

    /**
     * Get whether or not this status is defined in the HTTP/1.1 specification.
     * <p/>
     * @return true if this status is defined in the HTTP/1.1 specification, false otherwise.
     */
    public boolean isHttpVersion11() {
        return Lazy.HTTP_1_1.contains(this);
    }

//    /**
//     * Get the class of this status code.
//     * <p/>
//     * @return status class
//     */
//    public Type type() {
//        return statusClass;
//    }
//
//    /**
//     * Get whether or not this status is within the information class range 100-199 (inclusive).
//     * <p/>
//     * @return true if this status is an informational, false otherwise.
//     */
//    public boolean isInformational() {
//        return statusClass == Type.Informational;
//    }
//
//    /**
//     * Get whether or not this status is within the success class range 200-299 (inclusive).
//     * <p/>
//     * @return true if this status is a success, false otherwise.
//     */
//    public boolean isSuccess() {
//        return statusClass == Type.Success;
//    }
//
//    /**
//     * Get whether or not this status is within the redirection class range 300-399 (inclusive).
//     * <p/>
//     * @return true if this status is a redirection, false otherwise.
//     */
//    public boolean isRedirection() {
//        return statusClass == Type.Redirection;
//    }
//
//    /**
//     * Get whether or not this status is within the client error class range 400-499 (inclusive).
//     * <p/>
//     * @return true if this status is a client error, false otherwise.
//     */
//    public boolean isClientError() {
//        return statusClass == Type.Client_Error;
//    }
//
//    /**
//     * Get whether or not this status is within the server error class range 500-599 (inclusive).
//     * <p/>
//     * @return true if this status is a server error, false otherwise.
//     */
//    public boolean isServerError() {
//        return statusClass == Type.Server_Error;
//    }
//
//    /**
//     * Get whether or this status indicates some sort of error has occurred.
//     * <p/>
//     * @return true if an error has occurred, false otherwise.
//     */
//    public boolean isError() {
//        return isClientError() || isServerError();
//    }

    @Override
    public String toString() {
        return String.format("[%03d %s]", code(), message());
    }

    private static final MessageFormat HTML_ERROR_TEMPLATE = new MessageFormat(
            "<!doctype html>\n"
            + "<html>"
            + "  <head>\n"
            + "    <title>{0,number,integer} {1}</title>\n"
            + "  </head>"
            + "  <body>\n"
            + "    <h1>{1}</h1>\n"
            + "    <p>{2}</p>\n"
            + "    <hr>\n"
            + "    <address>Unicorn powered magic webserver.</address>\n"
            + "  </body>"
            + "</html>");

    /**
     * Get a human readable HTML page describing this status.
     * <p/>
     * @param description elaboration of the status
     * @return string containing HTML page
     */
    public String toHtmlString(String description) {
        return HTML_ERROR_TEMPLATE.format(new Object[]{
                    code(),
                    StringEscapeUtils.escapeXml(message),
                    StringEscapeUtils.escapeXml(description),
                });
    }

    /**
     * Get the status object associated with this integer code.
     * <p/>
     * Note that there may be more than one status object per code. In this case the last to be
     * defined is returned by this method.
     * <p/>
     * @param code integer status code
     * @return status object for given code, or NullStatus if no status is defined for that code
     * @throws IllegalArgumentException if code is negative
     */
    public HttpStatus valueOf(int code) {
        Preconditions.checkArgument(
                code >= 0, "Expecting positive argument code, but found %s.", code);
        return Lazy.codeMap.containsKey(code) ? Lazy.codeMap.get(code) : NullStatus;
    }

//    /*
//     * Static utility methods
//     */
//    /**
//     * Get whether or node the given integer status code is within the informational class range
//     * 100-199 (inclusive).
//     * <p/>
//     * @param code integer status code
//     * @return true if the code is within the informational class, false otherwise
//     * @throws IllegalArgumentException if code is negative
//     */
//    public static boolean isInformational(int code) {
//        Preconditions.checkArgument(
//                code >= 0, "Expecting positive argument code, but found %s.", code);
//        return Type.Informational.contains(code);
//    }
//
//    /**
//     * Get whether or node the given integer status code is within the success class range 200-299
//     * (inclusive).
//     * <p/>
//     * @param code integer status code
//     * @return true if the code is within the success class, false otherwise
//     * @throws IllegalArgumentException if code is negative
//     */
//    public static boolean isSuccess(int code) {
//        Preconditions.checkArgument(
//                code >= 0, "Expecting positive argument code, but found %s.", code);
//        return Type.Success.contains(code);
//    }
//
//    /**
//     * Get whether or node the given integer status code is within the redirection class range
//     * 300-399 (inclusive).
//     * <p/>
//     * @param code integer status code
//     * @return true if the code is within the redirection class, false otherwise
//     * @throws IllegalArgumentException if code is negative
//     */
//    public static boolean isRedirection(int code) {
//        Preconditions.checkArgument(
//                code >= 0, "Expecting positive argument code, but found %s.", code);
//        return Type.Redirection.contains(code);
//    }
//
//    /**
//     * Get whether or node the given integer status code is within the client error class range
//     * 400-499 (inclusive).
//     * <p/>
//     * @param code integer status code
//     * @return true if the code is within the client error class, false otherwise
//     * @throws IllegalArgumentException if code is negative
//     */
//    public static boolean isClientError(int code) {
//        Preconditions.checkArgument(
//                code >= 0, "Expecting positive argument code, but found %s.", code);
//        return Type.Client_Error.contains(code);
//    }
//
//    /**
//     * Get whether or node the given integer status code is within the server error class range
//     * 500-599 (inclusive).
//     * <p/>
//     * @param code integer status code
//     * @return true if the code is within the server error class, false otherwise
//     * @throws IllegalArgumentException if code is negative
//     */
//    public static boolean isServerError(int code) {
//        Preconditions.checkArgument(
//                code >= 0, "Expecting positive argument code, but found %s.", code);
//        return Type.Server_Error.contains(code);
//    }

    /*
     * ***************************************************************************************
     *
     * Static constants that will be initialised when first used (as opposed to when the HttpStatus
     * enum is first used.
     *
     * ***************************************************************************************
     */
    private static class Lazy {

        private Lazy() {
            throw new AssertionError();
        }

        /**
         * Status codes defined by HTTP/1.0
         */
        private static final Set<HttpStatus> HTTP_1_0 = Sets.immutableEnumSet(
                OK, Created, Accepted, No_Content, Multiple_Choices, Moved_Permanently,
                Moved_Temporarily, Not_Modified, Bad_Request, Unauthorized, Forbidden, Not_Found,
                Internal_Server_Error, Not_Implemented, Bad_Gateway, Service_Unavailable);

        /**
         * Status codes defined by HTTP/1.1
         */
        private static final Set<HttpStatus> HTTP_1_1 = Sets.immutableEnumSet(
                Continue, Switching_Protocols, OK, Created, Accepted, NonAuthoritative_Information,
                No_Content, Reset_Content, Partial_Content, Multiple_Choices, Moved_Permanently,
                Found, See_Other, Not_Modified, Use_Proxy, Temporary_Redirect, Bad_Request,
                Unauthorized, Payment_Required, Forbidden, Not_Found, Method_Not_Allowed,
                Not_Acceptable, Proxy_Authentication_Required, Request_Timeout, Conflict, Gone,
                Length_Required, Precondition_Failed, Request_Entity_Too_Large, RequestURI_Too_Long,
                Unsupported_Media_Type, Requested_Range_Not_Satisfiable, Expectation_Failed,
                Internal_Server_Error, Not_Implemented, Bad_Gateway, Service_Unavailable,
                Gateway_Timeout, HTTP_Version_Not_Supported);

        /**
         * Mapping from integer codes to status objects.
         */
        private static final Map<Integer, HttpStatus> codeMap;

        static {
            ImmutableMap.Builder<Integer, HttpStatus> builder = ImmutableMap.builder();
            for (HttpStatus statusCode : HttpStatus.values())
                builder.put(statusCode.code(), statusCode);
            codeMap = builder.build();
        }
    }
    /*
     * ***************************************************************************************
     */

    /**
     * Constants enumerating the HTTP status code classes.
     */
//    public enum Type {
//
//        /**
//         * 1xx Informational.
//         * <p/>
//         * Request received, continuing process.[2]
//         * <p/>
//         * This class of status code indicates a provisional response, consisting only of the
//         * Status-Line and optional headers, and is terminated by an empty line. Since HTTP/1.0 did
//         * not define any 1xx status statuses, servers must not send a 1xx response to an HTTP/1.0
//         * client except under experimental conditions.
//         */
//        Informational(100, 199, "Informational"),
//        /**
//         * 2xx Success.
//         * <p/>
//         * This class of status statuses indicates the action requested by the client was received,
//         * understood, accepted and processed successfully.
//         */
//        Success(200, 299, "Redirection"),
//        /**
//         * 3xx Redirection.
//         * <p/>
//         * The client must take additional action to complete the request.[2] This class of status
//         * code indicates that further action needs to be taken by the user agent in order to fulfil
//         * the request. The action required may be carried out by the user agent without interaction
//         * with the user if and only if the method used in the second request is GET or HEAD. A user
//         * agent should not automatically redirect a request more than five times, since such
//         * redirections usually indicate an infinite loop.
//         */
//        Redirection(300, 399, "Client Error"),
//        /**
//         * 4xx Client Error.
//         * <p/>
//         * The 4xx class of status code is intended for cases in which the client seems to have
//         * erred. Except when responding to a HEAD request, the server should include an entity
//         * containing an explanation of the error situation, and whether it is a temporary or
//         * permanent condition. These status statuses are applicable to any request method. User
//         * agents should display any included entity to the user.
//         */
//        Client_Error(400, 499, "Server Error"),
//        /**
//         * 5xx Server Error.
//         * <p/>
//         * The server failed to fulfill an apparently valid request.[2] Response status statuses
//         * beginning with the digit "5" indicate cases in which the server is aware that it has
//         * encountered an error or is otherwise incapable of performing the request. Except when
//         * responding to a HEAD request, the server should include an entity containing an
//         * explanation of the error situation, and indicate whether it is a temporary or permanent
//         * condition. Likewise, user agents should display any included entity to the user. These
//         * response statuses are applicable to any request method.
//         */
//        Server_Error(500, 599, ""),;
//
//        /**
//         * Starting integer code range value.
//         */
//        private final int start;
//
//        /**
//         * Ending integer code range value.
//         */
//        private final int end;
//
//        /**
//         * Human readable message.
//         */
//        private final String message;
//
//        /**
//         * Collection of all status objects of this type.
//         */
//        private final Set<HttpStatus> codes;
//
//        /**
//         * Private constructor.
//         * <p/>
//         * @param start   first integer code in range
//         * @param end     last integer code in range
//         * @param message human readable text message
//         * @throws IllegalArgumentException if start or end is negative, or start is greater than
//         *                                  end
//         * @throws NullPointerException     if message is null
//         */
//        private Type(int start, int end, String message) {
//            Preconditions.checkArgument(
//                    start >= 0, "Expecting positive argument start, but found %s.", start);
//            Preconditions.checkArgument(
//                    end >= 0, "Expecting positive argument end, but found %s.", end);
//            Preconditions.checkArgument(
//                    start <= end, "Expecting arguments start < end, but found %s > %s.", start, end);
//            Preconditions.checkNotNull(message, "message");
//            this.start = start;
//            this.end = end;
//            this.message = message;
//            codes = getStatusesInRange(start, end);
//        }
//
//        /**
//         * Get the start of the integer code range defined by this status class.
//         * <p/>
//         * @return start of code range
//         */
//        public int start() {
//            return start;
//        }
//
//        /**
//         * Get the end of the integer code range defined by this status class.
//         * <p/>
//         * @return end of code range
//         */
//        public int end() {
//            return end;
//        }
//
//        /**
//         * Get a collection of all statuses that are within this class.
//         * <p/>
//         * @return collection of statuses within this class.
//         */
//        public Set<HttpStatus> statuses() {
//            return codes;
//        }
//
//        /**
//         * Get a human readable description of this status class.
//         * <p/>
//         * @return a human readable description
//         */
//        public String message() {
//            return message;
//        }
//
//        /**
//         * Get whether or not the given integer status code is within the code range defined by this
//         * status class.
//         * <p/>
//         * @param code integer status code to check
//         * @return true if code is within the class range, false otherwise.
//         * @throws IllegalArgumentException if code is negative
//         */
//        public boolean contains(int code) {
//            Preconditions.checkArgument(
//                    code >= 0, "Expecting positive argument code, but found %s.", code);
//            return (start <= code) && (code <= end);
//        }
//
//        @Override
//        public String toString() {
//            return String.format("[%03d-%03d %s]", start(), end(), message());
//        }
//    }
//
//    /*
//     * ***************************************************************************************
//     * Private utilities used to initialize the instances
//     * ***************************************************************************************
//     */
//    /**
//     * Get the status class that contains the given code.
//     * <p/>
//     * @param code code to find the class of
//     * @return class containing the status code
//     * @throw AssertionError if there is no class for the given code
//     */
//    private static Type getTypeOfCode(int code) {
//        for (Type codeClass : Type.values())
//            if (codeClass.contains(code))
//                return codeClass;
//        throw new AssertionError();
//    }

    /**
     * Get a collection of all status objects with a code within the given range inclusively.
     * <p/>
     * @param min starting status code (inclusive)
     * @param max starting status code (inclusive)
     * @return set of all status objects with a code in the given range
     * @throws IllegalArgumentException if either argument is negative or if min is greater than max
     */
    private static Set<HttpStatus> getStatusesInRange(int min, int max) {
        Preconditions.checkArgument(
                min >= 0, "Expecting positive argument min, but found %s.", min);
        Preconditions.checkArgument(
                max >= 0, "Expecting positive argument max, but found %s.", max);
        Preconditions.checkArgument(
                min <= max, "Expecting arguments min < max, but found %s > %s.", min, max);
        EnumSet<HttpStatus> result = EnumSet.<HttpStatus>noneOf(HttpStatus.class);
        for (HttpStatus status : HttpStatus.values())
            if (min <= status.code() && max >= status.code())
                result.add(status);
        return Sets.immutableEnumSet(result);
    }
}
