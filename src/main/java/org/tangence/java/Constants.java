package org.tangence.java;
/**
 * Automatically generated - edit at your own risk
 *
 * Last update was probably 2014-04-04T18:49:54
 */

public class Constants {
	/** Version constants */
	public static final byte MAJOR_VERSION = 0;
	public static final byte MINOR_VERSION = 4;
	public static final byte MINOR_VERSION_MIN = 2;

	public static TangenceMessage classFromType(final int type, final long length, final Registry registry) throws TangenceException {
		switch(type) {
		case MSG_CALL: return new TangenceMessageCall(type, length, registry);
		case MSG_DESTROY: return new TangenceMessageDestroy(type, length, registry);
		case MSG_ERROR: return new TangenceMessageError(type, length, registry);
		case MSG_EVENT: return new TangenceMessageEvent(type, length, registry);
		case MSG_GETPROP: return new TangenceMessageGetProp(type, length, registry);
		case MSG_GETPROPELEM: return new TangenceMessageGetPropElem(type, length, registry);
		case MSG_GETREGISTRY: return new TangenceMessageGetRegistry(type, length, registry);
		case MSG_GETROOT: return new TangenceMessageGetRoot(type, length, registry);
		case MSG_INIT: return new TangenceMessageInit(type, length, registry);
		case MSG_INITED: return new TangenceMessageInited(type, length, registry);
		case MSG_ITER_DESTROY: return new TangenceMessageIterDestroy(type, length, registry);
		case MSG_ITER_NEXT: return new TangenceMessageIterNext(type, length, registry);
		case MSG_ITER_RESULT: return new TangenceMessageIterResult(type, length, registry);
		case MSG_OK: return new TangenceMessageOK(type, length, registry);
		case MSG_RESULT: return new TangenceMessageResult(type, length, registry);
		case MSG_SETPROP: return new TangenceMessageSetProp(type, length, registry);
		case MSG_SUBSCRIBE: return new TangenceMessageSubscribe(type, length, registry);
		case MSG_SUBSCRIBED: return new TangenceMessageSubscribed(type, length, registry);
		case MSG_UNSUBSCRIBE: return new TangenceMessageUnsubscribe(type, length, registry);
		case MSG_UNWATCH: return new TangenceMessageUnwatch(type, length, registry);
		case MSG_UPDATE: return new TangenceMessageUpdate(type, length, registry);
		case MSG_WATCH: return new TangenceMessageWatch(type, length, registry);
		case MSG_WATCH_ITER: return new TangenceMessageWatchIter(type, length, registry);
		case MSG_WATCHING: return new TangenceMessageWatching(type, length, registry);
		case MSG_WATCHING_ITER: return new TangenceMessageWatchingIter(type, length, registry);
		default: throw new TangenceException("Unknown type " + String.valueOf(type));
		}
	}

	/** Defined message types - this is the first byte in a message packet */
	public static final int MSG_CALL                 = 0x01;
	public static final int MSG_SUBSCRIBE            = 0x02;
	public static final int MSG_UNSUBSCRIBE          = 0x03;
	public static final int MSG_EVENT                = 0x04;
	public static final int MSG_GETPROP              = 0x05;
	public static final int MSG_SETPROP              = 0x06;
	public static final int MSG_WATCH                = 0x07;
	public static final int MSG_UNWATCH              = 0x08;
	public static final int MSG_UPDATE               = 0x09;
	public static final int MSG_DESTROY              = 0x0a;
	public static final int MSG_GETPROPELEM          = 0x0b;
	public static final int MSG_WATCH_ITER           = 0x0c;
	public static final int MSG_ITER_NEXT            = 0x0d;
	public static final int MSG_ITER_DESTROY         = 0x0e;
	public static final int MSG_GETROOT              = 0x40;
	public static final int MSG_GETREGISTRY          = 0x41;
	public static final int MSG_INIT                 = 0x7f;
	public static final int MSG_OK                   = 0x80;
	public static final int MSG_ERROR                = 0x81;
	public static final int MSG_RESULT               = 0x82;
	public static final int MSG_SUBSCRIBED           = 0x83;
	public static final int MSG_WATCHING             = 0x84;
	public static final int MSG_WATCHING_ITER        = 0x85;
	public static final int MSG_ITER_RESULT          = 0x86;
	public static final int MSG_INITED               = 0xff;

	public static final int DATA_NUMBER              = 0x00;
	public static final int DATA_STRING              = 0x01;
	public static final int DATA_LIST                = 0x02;
	public static final int DATA_DICT                = 0x03;
	public static final int DATA_OBJECT              = 0x04;
	public static final int DATA_RECORD              = 0x05;
	public static final int DATA_META                = 0x07;
	public static final int DATAMETA_CONSTRUCT       = 0x01;
	public static final int DATAMETA_CLASS           = 0x02;
	public static final int DATAMETA_STRUCT          = 0x03;
	public static final int DATANUM_BOOLFALSE        = 0x00;
	public static final int DATANUM_BOOLTRUE         = 0x01;
	public static final int DATANUM_UINT8            = 0x02;
	public static final int DATANUM_SINT8            = 0x03;
	public static final int DATANUM_UINT16           = 0x04;
	public static final int DATANUM_SINT16           = 0x05;
	public static final int DATANUM_UINT32           = 0x06;
	public static final int DATANUM_SINT32           = 0x07;
	public static final int DATANUM_UINT64           = 0x08;
	public static final int DATANUM_SINT64           = 0x09;
	public static final int DATANUM_FLOAT16          = 0x10;
	public static final int DATANUM_FLOAT32          = 0x11;
	public static final int DATANUM_FLOAT64          = 0x12;

	/** Change notifications */
	public static final int CHANGE_SET               = 0x01;
	public static final int CHANGE_ADD               = 0x02;
	public static final int CHANGE_DEL               = 0x03;
	public static final int CHANGE_PUSH              = 0x04;
	public static final int CHANGE_SHIFT             = 0x05;
	public static final int CHANGE_SPLICE            = 0x06;
	public static final int CHANGE_MOVE              = 0x07;

	/** Property dimension sizes */
	public static final int DIM_SCALAR               = 0x01;
	public static final int DIM_HASH                 = 0x02;
	public static final int DIM_QUEUE                = 0x03;
	public static final int DIM_ARRAY                = 0x04;
	public static final int DIM_OBJSET               = 0x05;

	/** Property dimension sizes */
	public static final int ITER_FWD                 = 0x01;
	public static final int ITER_FIRST               = 0x01;
	public static final int ITER_LAST                = 0x02;
	public static final int ITER_BACK                = 0x02;

	/** Returns the string equivalent for a message ID */
	public static String messageName(final int id) {
		switch(id) {
		case MSG_CALL: return "msg_call";
		case MSG_SUBSCRIBE: return "msg_subscribe";
		case MSG_UNSUBSCRIBE: return "msg_unsubscribe";
		case MSG_EVENT: return "msg_event";
		case MSG_GETPROP: return "msg_getprop";
		case MSG_SETPROP: return "msg_setprop";
		case MSG_WATCH: return "msg_watch";
		case MSG_UNWATCH: return "msg_unwatch";
		case MSG_UPDATE: return "msg_update";
		case MSG_DESTROY: return "msg_destroy";
		case MSG_GETPROPELEM: return "msg_getpropelem";
		case MSG_WATCH_ITER: return "msg_watch_iter";
		case MSG_ITER_NEXT: return "msg_iter_next";
		case MSG_ITER_DESTROY: return "msg_iter_destroy";
		case MSG_GETROOT: return "msg_getroot";
		case MSG_GETREGISTRY: return "msg_getregistry";
		case MSG_INIT: return "msg_init";
		case MSG_OK: return "msg_ok";
		case MSG_ERROR: return "msg_error";
		case MSG_RESULT: return "msg_result";
		case MSG_SUBSCRIBED: return "msg_subscribed";
		case MSG_WATCHING: return "msg_watching";
		case MSG_WATCHING_ITER: return "msg_watching_iter";
		case MSG_ITER_RESULT: return "msg_iter_result";
		case MSG_INITED: return "msg_inited";
		default: break;
		}
		return "unknown";
	}
};
