/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Android\\DEMO\\EventPlannerApp\\EventPlannerService\\src\\com\\ipc\\eventplannerservice\\IEventPlannerService.aidl
 */
package com.ipc.eventplannerservice;
public interface IEventPlannerService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.ipc.eventplannerservice.IEventPlannerService
{
private static final java.lang.String DESCRIPTOR = "com.ipc.eventplannerservice.IEventPlannerService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.ipc.eventplannerservice.IEventPlannerService interface,
 * generating a proxy if needed.
 */
public static com.ipc.eventplannerservice.IEventPlannerService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.ipc.eventplannerservice.IEventPlannerService))) {
return ((com.ipc.eventplannerservice.IEventPlannerService)iin);
}
return new com.ipc.eventplannerservice.IEventPlannerService.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_sendNewEvent:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
int _result = this.sendNewEvent(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_sendEventReply:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
int _result = this.sendEventReply(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_sendEventConfirmation:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
int _result = this.sendEventConfirmation(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.ipc.eventplannerservice.IEventPlannerService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public int sendNewEvent(java.lang.String phoneNumberList, java.lang.String newEventSMS) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(phoneNumberList);
_data.writeString(newEventSMS);
mRemote.transact(Stub.TRANSACTION_sendNewEvent, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int sendEventReply(java.lang.String phoneNumber, java.lang.String eventReplySMS) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(phoneNumber);
_data.writeString(eventReplySMS);
mRemote.transact(Stub.TRANSACTION_sendEventReply, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int sendEventConfirmation(java.lang.String phoneNumberList, java.lang.String eventConfirmationSMS) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(phoneNumberList);
_data.writeString(eventConfirmationSMS);
mRemote.transact(Stub.TRANSACTION_sendEventConfirmation, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_sendNewEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_sendEventReply = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_sendEventConfirmation = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public int sendNewEvent(java.lang.String phoneNumberList, java.lang.String newEventSMS) throws android.os.RemoteException;
public int sendEventReply(java.lang.String phoneNumber, java.lang.String eventReplySMS) throws android.os.RemoteException;
public int sendEventConfirmation(java.lang.String phoneNumberList, java.lang.String eventConfirmationSMS) throws android.os.RemoteException;
}
