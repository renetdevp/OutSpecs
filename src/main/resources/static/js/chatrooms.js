const stompClient = new StompJs.Client({
  brokerURL: 'ws://localhost:8080/ws',
  // csrf: [[${_csrf}]],
});

stompClient.onConnect = (frame) => {
  stompClient.subscribe(`/queue/users/${USER_ID}`, async (msg) => {
    await onMessage(msg);
  }, { userId: USER_ID });
};

stompClient.onWebSocketError = (error) => {
  console.error('error with websocket', error);
  stompClient.deactivate();
};

stompClient.onStompError = (frame) => {
  console.error('Broker reported error: ' + frame.headers['message']);
  console.error('Additional details: ' + frame.body);
  stompClient.deactivate();
};

const chatMessageCursors = {};
const DEFAULT_USER_PROFILE_URL = '/images/user_default_img.svg';

async function onMessage(msg){
  const { chatRoomId, senderId, content, createdAt } = JSON.parse(msg.body);
  let chatMessagesElem = document.querySelector(`.chat-message-container-${chatRoomId} > .chat-messages`);

  if (!chatMessagesElem){
    await insertNewChatRoom(chatRoomId);
    insertChatMessageContainer(chatRoomId);
    chatMessagesElem = document.querySelector(`.chat-message-container-${chatRoomId} > .chat-messages`);
  }

  insertChatMessage(chatMessagesElem, senderId, content, createdAt, 'beforeend');
  chatMessagesElem.scrollTop = chatMessagesElem.scrollHeight;

  if (!chatMessageCursors[`chatRoom-${chatRoomId}`]) chatMessageCursors[`chatRoom-${chatRoomId}`] = createdAt;

  document.querySelector(`.chatroom[data-chatroom-id="${chatRoomId}"] > .chatroom-info > .last-message`).textContent = content;
  updateUpdatedAt(chatRoomId, createdAt);
}

/**
 * 채팅 메시지(div.chat-message)를 parent 내부에 추가하는 함수
 * @param {HTMLElement} parent 
 * @param {number} senderId 
 * @param {string} content 
 * @param {string} createdAt 
 * @param {string} position 
 */
function insertChatMessage(parent, senderId, content, createdAt, position){
  const newChat = document.createElement('div');

  newChat.className = 'chat-message';
  newChat.setAttribute('self', USER_ID === senderId);
  newChat.textContent = getDString({ hour: 'numeric', minute: 'numeric' }, createdAt);

  const newChatContent = document.createElement('span');
  newChatContent.className = 'chat-message-content';
  newChatContent.textContent = content;
  newChat.insertAdjacentElement('beforeend', newChatContent);

  parent.insertAdjacentElement(position, newChat);
}

/**
 * .chatroom의 updateAt을 최신화하는 함수
 * @param {number} chatRoomId 
 * @param {string} createdAt 
 */
function updateUpdatedAt(chatRoomId, createdAt){
  const dString = getDString({ hour: 'numeric', minute: 'numeric' }, createdAt);
  document.querySelector(`.chatroom[data-chatroom-id="${chatRoomId}"] > .chatroom-info-updated-at`).textContent = dString;
}

/**
 * 채팅 메시지를 적재할 container를 추가하는 함수
 * @param {number} chatRoomId 
 */
function insertChatMessageContainer(chatRoomId){
  const newMessageContainer = document.createElement('div');
  newMessageContainer.classList.add(`chat-message-container-${chatRoomId}`, 'chat-message-container');
  newMessageContainer.setAttribute('disabled', true);

  insertNewChatMessage(newMessageContainer);
  insertMessageInput(newMessageContainer, chatRoomId);

  document.querySelector('.chatroom-page-content').insertAdjacentElement('beforeend', newMessageContainer);
}

/**
 * 채팅 메시지의 내용들이 들어갈 .chat-messages를 추가하는 함수
 * @param {HTMLElement} parent 
 */
function insertNewChatMessage(parent){
  const newChatMessages = document.createElement('div');

  newChatMessages.className = 'chat-messages';

  parent.insertAdjacentElement('beforeend', newChatMessages);
}

/**
 * 채팅 메시지를 입력받을 .message-input을 추가하는 함수
 * @param {HTMLElement} parent 
 * @param {number} chatRoomId 
 */
function insertMessageInput(parent, chatRoomId){
  const newMessageInput = document.createElement('input');

  newMessageInput.className = 'message-input';
  newMessageInput.placeholder = '채팅 메시지 입력';

  newMessageInput.addEventListener('keydown', (e) => {
    messageInputOnKeyDown(chatRoomId, e);
  });

  parent.insertAdjacentElement('beforeend', newMessageInput);
}

/**
 * .message-input의 keydown 이벤트를 처리할 핸들러 함수
 * @param {number} chatRoomId 
 * @param {KeyboardEvent} e 
 * @returns 
 */
function messageInputOnKeyDown(chatRoomId, e){
  if (e.key !== 'Enter') return;

  const msg = e.target.value;

  if (msg === '') return;

  e.target.value = '';

  stompClient.publish({
    destination: `/app/chats/${chatRoomId}`,
    body: JSON.stringify({ content: msg }),
    headers: {
      'userId': USER_ID,
    },
  });
}

/**
 * .chatroom의 onclick 이벤트를 처리할 핸들러 함수
 * @param {MouseEvent} e 
 */
function chatRoomOnClick(e){
  const chatRoomId = e.currentTarget.getAttribute('data-chatroom-id');

  const elem = document.querySelector('.chat-message-container[disabled=false]')
  if (!!elem) elem.setAttribute('disabled', true);

  document.querySelector(`.chat-message-container-${chatRoomId}`).setAttribute('disabled', false);

  const chatMessagesElem = document.querySelector(`.chat-message-container-${chatRoomId} > .chat-messages`);
  chatMessagesElem.scrollTop = chatMessagesElem.scrollHeight;
}

function getDString(format, date){
  return Intl.DateTimeFormat('ko', format).format(new Date(date));
}

/**
 * 스크롤 이벤트의 과도한 호출 방지를 위한 스로틀링 함수
 * @param {Function} func 
 * @param {number} delay 
 * @returns {Function}
 */
function throttle(func, delay){
  let inThrottle = false;

  return function (){
    const context = this;
    const args = arguments;

    if (!inThrottle){
      func.apply(context, args);
      inThrottle = true;
      setTimeout(() => inThrottle = false, delay);
    }
  }
}

/**
 * 무한 스크롤을 위한 추가 메시지를 요청하는 함수
 * @param {number} chatRoomId 
 * @returns 
 */
async function fetchMoreMessages(chatRoomId){
  const target = document.querySelector(`.chat-message-container-${chatRoomId} > .chat-messages`);

  if (!target || target.scrollTop > 0) return;

  let url = `/chats/${chatRoomId}/messages`;
  const firstCreatedAt = chatMessageCursors[`chatRoom-${chatRoomId}`];
  if (!!firstCreatedAt) url += `?firstCreatedAt=${firstCreatedAt}`;

  const res = await fetch(url);

  if (!res.ok) return;

  const jsonData = await res.json();

  console.log(jsonData);

  if (!(jsonData instanceof Array) || jsonData.length === 0) return;

  chatMessageCursors[`chatRoom-${chatRoomId}`] = jsonData[jsonData.length-1].createdAt;

  const oldHeight = target.scrollHeight;

  for (let chatMessage of jsonData){
    const { senderId, content, createdAt } = chatMessage;

    insertChatMessage(target, senderId, content, createdAt, 'afterbegin');
  }

  target.scrollTop = target.scrollHeight - oldHeight;
}

function disableMessageInput(){
  document.querySelector('.message-input').disabled = true;
}

/**
 * 새로운 .chatroom을 추가하는 함수
 * @param {number} chatRoomId 
 */
async function insertNewChatRoom(chatRoomId){
  const chatRoomInfo = await fetchChatRoomInfo(chatRoomId);

  const newChatRoom = document.createElement('article');
  newChatRoom.className = 'chatroom';
  newChatRoom.setAttribute('data-chatroom-id', chatRoomId);

  let targetImgUrl, targetNickname, lastMessage;
  if (USER_ID === chatRoomInfo?.user1Id){
    targetImgUrl = chatRoomInfo?.user2ImageUrl;
    targetNickname = chatRoomInfo?.user2Nickname;
  }else if (USER_ID === chatRoomInfo?.user2Id){
    targetImgUrl = chatRoomInfo?.user1ImageUrl;
    targetNickname = chatRoomInfo?.user2Nickname;
  }
  lastMessage = chatRoomInfo?.chatRoomLastMessage;

  const newProfileImage = createChatRoomProfileImage(targetImgUrl);
  const newChatRoomInfo = createChatRoomInfo(targetNickname, lastMessage);

  const newUpdatedAt = document.createElement('div');
  newUpdatedAt.className = 'chatroom-info-updated-at';

  newChatRoom.insertAdjacentElement('beforeend', newProfileImage);
  newChatRoom.insertAdjacentElement('beforeend', newChatRoomInfo);
  newChatRoom.insertAdjacentElement('beforeend', newUpdatedAt);

  newChatRoom.addEventListener('click', chatRoomOnClick);

  document.querySelector('.chatroom-page-content > .chatroom-container').insertAdjacentElement('afterbegin', newChatRoom);
}

/**
 * 새로운 .chatroom 추가를 위한 채팅방 정보 요청
 * @param {number} chatRoomId 
 */
async function fetchChatRoomInfo(chatRoomId){
  const url = `/chats/${chatRoomId}`;

  const res = await fetch(url);

  if (!res.ok) return null;

  const jsonData = await res.json();

  console.log(jsonData);

  return jsonData;
}

function createChatRoomProfileImage(profileImgUrl){
  const newProfileImage = document.createElement('div');
  newProfileImage.className = 'chatroom-info-profile-image';

  const img = document.createElement('img');
  img.className = 'target-image';
  img.src = !profileImgUrl ? DEFAULT_USER_PROFILE_URL : profileImgUrl;

  newProfileImage.insertAdjacentElement('afterbegin', img);

  return newProfileImage;
}

function createChatRoomInfo(targetNickname, lastMessage){
  const newChatRoomInfo = document.createElement('div');
  newChatRoomInfo.className = 'chatroom-info';

  const targetNicknameElem = document.createElement('div');
  targetNicknameElem.className = 'target-nickname';
  targetNicknameElem.textContent = targetNickname;

  const lastMessageElem = document.createElement('div');
  lastMessageElem.className = 'last-message';
  lastMessageElem.textContent = lastMessage;

  newChatRoomInfo.insertAdjacentElement('beforeend', targetNicknameElem);
  newChatRoomInfo.insertAdjacentElement('beforeend', lastMessageElem);

  return newChatRoomInfo;
}

window.addEventListener('load', (e) => {
  stompClient.activate();

  // 채팅방 onClick 이벤트 설정
  const chatRoomElems = document.querySelectorAll('.chatroom');
  for (let chatroom of chatRoomElems){
    chatroom.addEventListener('click', chatRoomOnClick);
  }

  for (let chatroom of chatRooms){
    insertChatMessageContainer(chatroom.chatRoomId);

    // 채팅메시지 무한스크롤 구현을 위한 이벤트 리스너
    const chatMessagesElem = document.querySelector(`.chat-message-container-${chatroom.chatRoomId} > .chat-messages`);
    const fetchMessage = throttle(() => { fetchMoreMessages(chatroom.chatRoomId); }, 500);
    chatMessagesElem.addEventListener('scroll', (e) => {
      if (!e.currentTarget || e.currentTarget.scrollTop > 0) return;

      fetchMessage();
    });

    const updatedAt = chatroom.chatRoomLastMessageCreatedAt;

    if (!updatedAt) continue;

    const dString = getDString({ hour: 'numeric', minute: 'numeric' }, updatedAt);
    document.querySelector(`.chatroom[data-chatroom-id="${chatroom.chatRoomId}"] .chatroom-info-updated-at`).textContent = dString;

    if (chatroom.chatMessageDTOList instanceof Array && chatroom.chatMessageDTOList.length !== 0){
      for (let chatMessage of chatroom.chatMessageDTOList){
        const { senderId, content, createdAt } = chatMessage;

        insertChatMessage(chatMessagesElem, senderId, content, createdAt, 'afterbegin');
      }

      chatMessageCursors[`chatRoom-${chatroom.chatRoomId}`] = chatroom.chatMessageDTOList[chatroom.chatMessageDTOList.length-1].createdAt;
    }
  }
});