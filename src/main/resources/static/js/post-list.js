// DOM이 로드된 후 실행
document.addEventListener('DOMContentLoaded', function() {
    // 태그 클릭 이벤트
    const container = document.querySelector('.container');
    const postTypePath = container.dataset.postType;

    document.querySelectorAll('.tag-item').forEach(tag => {
        tag.addEventListener('click', function() {
            if(postTypePath === 'play') {
                document.querySelectorAll('.tag-item').forEach(t => t.classList.remove('active'));
            }
            this.classList.toggle('active');
            const activeTags = Array.from(document.querySelectorAll('.tag-item.active'))
                                    .map(t => t.dataset.tag);
            const query = activeTags.map(t => `tags=${encodeURIComponent(t)}`).join('&');
            window.location.href = '/list/' + postTypePath + '/filter' + (query ? `?${query}` : '');
        });
    });

    // 무한스크롤
    let page = 0;
    const size = 5;
    let isLoading = false;
    let hasNext = true;

    const grid = document.querySelector('.post-list');
    const sentinel = document.createElement('div');
    sentinel.id = 'scroll-sentinel';
    grid.after(sentinel);

    // 현재 페이지 URL에서 쿼리 파라미터 가져오기
    const currentQuery = window.location.search; // ?tags=React&tags=Python
    const basePath = window.location.pathname;   // /list/free/filter

    const observer = new IntersectionObserver(async (entries) => {
        if (entries[0].isIntersecting && !isLoading && hasNext) {
            isLoading = true;
            page++;

            try {
                const url = `${basePath}${currentQuery}${currentQuery ? '&' : '?'}page=${page}&size=${size}&fragment=true`;
                const res = await fetch(url);
                const html = await res.text();

                const temp = document.createElement('div');
                temp.innerHTML = html;

                const posts = temp.querySelectorAll('.post-item');
                if (posts.length === 0) {
                    hasNext = false;
                    observer.unobserve(sentinel);
                }

                posts.forEach(post => grid.appendChild(post));
            } catch (e) {
                console.error('게시글 로딩 실패', e);
            }

            isLoading = false;
        }
    }, { threshold: 1, rootMargin: '100px' });

    observer.observe(sentinel);

  function toggleChatBotContainer(e){
    const chatbotContainer = document.querySelector('.chatbot-container');

    if (chatbotContainer.style.display !== 'flex'){
      chatbotContainer.style.display = 'flex';
      e.currentTarget.textContent = 'X';
    }else {
      chatbotContainer.style.display = 'none';
      e.currentTarget.textContent = 'AI 챗봇';
    }
  }

  // AI 챗봇 클릭 이벤트
  document.querySelector('.ai-chat')?.addEventListener('click', toggleChatBotContainer);

  (function loadPostContent(){
    if (!window.location.href.includes('/list/ai-play')) return;

    const postContentBoxes = document.querySelectorAll('.post-content-box');

    for (let box of postContentBoxes){
        box.innerHTML = marked.parse(box.innerHTML);
    }
  })();
});