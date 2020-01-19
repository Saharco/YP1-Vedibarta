// ------------------------------ Initialization ------------------------------

const admin = require('firebase-admin');
const functions = require('firebase-functions');
admin.initializeApp(functions.config().firebase);

const firestoreTriggers = functions.region('europe-west1').firestore;
const db = admin.firestore();

const development = 'development';
const production = 'production';


// ------------------------------ Cloud functions' path triggers ------------------------------


// -------------------- GENERAL: --------------------

/**
 * HTTP trigger for new messages
 * @see onMessageSentHandler
 * @type {CloudFunction<DocumentSnapshot>}
 */
exports.generalOnMessageSent = firestoreTriggers.document('chats/{chatId}/messages/{messageId}').onCreate((snap, context) => {
    return onMessageSentHandler(snap, context, db);
});

/**
 * HTTP trigger for firing notifications for new messages
 * @see onMessageSentHandler
 * @type {CloudFunction<DocumentSnapshot>}
 */
exports.generalOnMessageSentNotify = firestoreTriggers.document('chats/{chatId}/messages/{messageId}').onCreate((snap, context) => {
    return onMessageSentNotifyHandler(snap, context, db);
});

// -------------------- DEVELOPMENT: --------------------

/**
 * HTTP trigger for new messages in the *development* path in the database
 * @see onMessageSentHandler
 * @type {CloudFunction<DocumentSnapshot>}
 */
exports.developmentOnMessageSent = firestoreTriggers.document('development/{version}/chats/{chatId}/messages/{messageId}').onCreate((snap, context) => {
    let root = db
        .collection(development)
        .doc(context.params.version);

    return onMessageSentHandler(snap, context, root);
});

/**
 * HTTP trigger for firing notifications for new messages in the *development* path in the database
 * @see onMessageSentHandler
 * @type {CloudFunction<DocumentSnapshot>}
 */
exports.developmentOnMessageSentNotify = firestoreTriggers.document('development/{version}/chats/{chatId}/messages/{messageId}').onCreate((snap, context) => {
    let root = db
        .collection(development)
        .doc(context.params.version);

    return onMessageSentNotifyHandler(snap, context, root);
});

// -------------------- PRODUCTION: --------------------

/**
 * HTTP trigger for new messages in the *production* path in the database
 * @see onMessageSentHandler
 * @type {CloudFunction<DocumentSnapshot>}
 */
exports.productionOnMessageSent = firestoreTriggers.document('production/{version}/chats/{chatId}/messages/{messageId}').onCreate((snap, context) => {
    let root = db
        .collection(production)
        .doc(context.params.version);

    return onMessageSentHandler(snap, context, root);
});


/**
 * HTTP trigger for firing notifications for new messages in the *production* path in the database
 * @see onMessageSentHandler
 * @type {CloudFunction<DocumentSnapshot>}
 */
exports.productionOnMessageSentNotify = firestoreTriggers.document('production/{version}/chats/{chatId}/messages/{messageId}').onCreate((snap, context) => {
    let root = db
        .collection(production)
        .doc(context.params.version);

    return onMessageSentNotifyHandler(snap, context, root);
});


// ------------------------------ Cloud functions' logic ------------------------------


/**
 * *Listens to new messages*.
 * - Increases a counter that represents the amount of messages in this chat room.
 * - Updates the timestamp of the last received message in this chat room.
 *
 * @type {CloudFunction<DocumentSnapshot>}
 */
function onMessageSentHandler(snap, context, root) {
    const chatDocRef = root
        .collection('chats')
        .doc(context.params.chatId);

    return db.runTransaction(function (transaction) {
        return transaction.get(chatDocRef).then(function (chatDoc) {
            if (!chatDoc.exists) {
                console.log("Error: chat room does not exist");
                return null;
            }

            let newNumMessages = chatDoc.data().numMessages + 1;
            let newLastMessageTimestamp = snap.data().timestamp;
            let newLastMessage = snap.data().text;

            transaction.update(chatDocRef, {numMessages: newNumMessages});
            transaction.update(chatDocRef, {lastMessageTimestamp: newLastMessageTimestamp});
            transaction.update(chatDocRef, {lastMessage: newLastMessage});
            return null;
        });
    }).then(function () {
        console.log("Updated messages counter");
        return null;
    }).catch(function (error) {
        console.log("Error updating messages counter: ", error);
        return null;
    });
}

/**
 * *Listens to new messages.*
 * - Sends a notification to the receiving end of a chat message
 *
 * @type {CloudFunction<DocumentSnapshot>}
 */
function onMessageSentNotifyHandler(snap, context, root) {
    return root.collection('students')
        .doc(snap.data().receiver)
        .get()
        .then(receiverDoc => {
            return root.collection('students')
                .doc(snap.data().sender)
                .get()
                .then(senderDoc => {
                    return root.collection('chats')
                        .doc(context.params.chatId)
                        .get()
                        .then(chatDoc => {
                            console.log('Creating a chat notification for: ', receiverDoc.data().name);
                            let tokens = receiverDoc.data().tokens;
                            let photo = "none";
                            if ('photo' in senderDoc.data() &&
                                senderDoc.data().photo !== 'undefined' &&
                                senderDoc.data().photo !== null) {
                                photo = senderDoc.data().photo;
                            }
                            const payload = {

                                data: {
                                    display_status: "admin_broadcast",
                                    notification_type: "CHAT",
                                    title: senderDoc.data().name,
                                    body: snap.data().text,
                                    sender_id: senderDoc.data().uid,
                                    sender_photo_url: photo,
                                    chat_id: chatDoc.data().chat,
                                }

                            };

                            console.log("Sending notification");
                            return admin.messaging().sendToDevice(tokens, payload)
                                .then(function (response) {
                                    console.log("Successfully sent chat notification\nResponse: ", response);
                                    return response;
                                })
                                .catch(function (error) {
                                    console.log("Error sending chat notification\nError message: ", error);
                                });
                        });
                });
        });
}