function getDString(time){
  return Intl.DateTimeFormat('ko', { hour: 'numeric', minute: 'numeric' }).format(time);
}

function insertChatMessage(parent, content, sender, createdAt, position){
  const newChatMessage = document.createElement('div');
  newChatMessage.className = 'chat-message';
  newChatMessage.setAttribute('self', sender === 'user');

  const newChatMessageContent = document.createElement('span');
  newChatMessageContent.className = 'chat-message-content';
  newChatMessageContent.textContent = content;

  const newTimestamp = document.createElement('span');
  newTimestamp.className = 'chat-message-timestamp';
  newTimestamp.textContent = createdAt;

  newChatMessage.insertAdjacentElement('beforeend', newChatMessageContent);
  newChatMessage.insertAdjacentElement('beforeend', newTimestamp);

  parent.insertAdjacentElement(position, newChatMessage);

  parent.scrollTop = parent.scrollHeight;
}

async function onQuestionInput(e){
  if (e.key !== 'Enter') return;

  const input = e.target.value.trim();

  if (!input) return;

  e.target.value = '';

  const chatMessageContainer = document.querySelector('.chat-message-container');
  insertChatMessage(chatMessageContainer, input, 'user', getDString(new Date()), 'beforeend');

  const result = await getAnswer(input);

  if (!!result.error) insertChatMessage(chatMessageContainer, result.error, 'chatbot', getDString(new Date()), 'beforeend');
  else insertChatMessage(chatMessageContainer, result.response, 'chatbot', getDString(new Date()), 'beforeend');
}

function setQuestionInputEventListener(){
  const target = document.querySelector('.chatbot-container > .question-input');

  target.addEventListener('keydown', onQuestionInput);
}

async function getAnswer(question){
  try {
    const res = await fetch(`/chats/alan?question=${question}&questionType=QUESTION`);

    if (!res.ok) return;

    return res.json();
  }catch (e){
    console.error(e);
    return { error: '챗봇 응답을 요청하던 중 오류가 발생했습니다!' };
  }
}

window.addEventListener('load', async () => {
  setQuestionInputEventListener();
});