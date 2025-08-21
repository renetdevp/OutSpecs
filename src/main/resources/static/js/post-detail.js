// More 메뉴 토글 함수
function toggleMoreMenu(event, menuId) {
    event.stopPropagation();
    const menu = document.getElementById(menuId);
    const allMenus = document.querySelectorAll('.more-menu, .comment-more-menu, .answer-more-menu');

    // 다른 메뉴들 닫기
    allMenus.forEach(m => {
        if (m.id !== menuId) {
            m.style.display = 'none';
        }
    });

    // 현재 메뉴 토글
    menu.style.display = menu.style.display === 'flex' ? 'none' : 'flex';
}

// 댓글 수정 함수
function editComment(button) {
    const commentItem = button.closest('.comment-item');
    const content = commentItem.querySelector('.comment-content');
    const editForm = commentItem.querySelector('.comment-edit-form');
    const moreMenu = button.closest('.comment-more-menu');

    content.style.display = 'none';
    editForm.style.display = 'block';
    moreMenu.style.display = 'none';
}

// 댓글 수정 취소 함수
function cancelEdit(button) {
    const commentItem = button.closest('.comment-item');
    const content = commentItem.querySelector('.comment-content');
    const editForm = commentItem.querySelector('.comment-edit-form');

    content.style.display = 'block';
    editForm.style.display = 'none';
}

// 대댓글 수정 함수
function editReply(button) {
    const replyItem = button.closest('.comment-reply-list');
    const content = replyItem.querySelector('.reply-content');
    const editForm = replyItem.querySelector('.reply-edit-form');
    const moreMenu = button.closest('.reply-more-menu');

    if (content && editForm) {
        content.style.display = 'none';
        editForm.style.display = 'block';
    }

    if (moreMenu) {
        moreMenu.style.display = 'none';
    }
}

// 대댓글 수정 취소 함수
function cancelReplyEdit(button) {
    const replyItem = button.closest('.comment-reply-list');
    const content = replyItem.querySelector('.reply-content');
    const editForm = replyItem.querySelector('.reply-edit-form');

    if (content && editForm) {
        content.style.display = 'block';
        editForm.style.display = 'none';
    }
}

// 댓글 답글 폼 표시 함수
function showReplyForm(commentId, toggleButton) {
    const replyForm = document.getElementById('reply-form-' + commentId);
    replyForm.style.display = 'block';
    toggleButton.style.display = 'none';
}

// 댓글 답글 폼 숨기기 함수
function hideReplyForm(commentId) {
    const replyForm = document.getElementById('reply-form-' + commentId);
    const commentItem = replyForm.closest('.comment-item'); // 댓글 전체 영역
    const toggleButton = commentItem.querySelector('.comment-btn-toggle'); // 버튼 찾기

    replyForm.style.display = 'none';
    if (toggleButton) {
        toggleButton.style.display = 'inline-block';
    }

    // 폼 내용 초기화
    const textarea = replyForm.querySelector('textarea');
    textarea.value = '';
}

// 답변 수정 함수 (HTML 구조에 맞게 수정)
function editAnswer(button) {
    const answerContainer = button.closest('.answer-post-container');
    const content = answerContainer.querySelector('.answer-content');
    const editForm = answerContainer.querySelector('.answer-edit-form');
    const moreMenu = button.closest('.more-menu');

    content.style.display = 'none';
    editForm.style.display = 'block';
    moreMenu.style.display = 'none';
}

// 답변 수정 취소 함수 (HTML 구조에 맞게 수정)
function cancelEditAnswer(button) {
    const answerContainer = button.closest('.answer-post-container');
    const content = answerContainer.querySelector('.answer-content');
    const editForm = answerContainer.querySelector('.answer-edit-form');

    content.style.display = 'block';
    editForm.style.display = 'none';
}

// 답변 댓글 폼 표시 함수
function showAnswerReplyForm(answerId, toggleButton) {
    const replyForm = document.getElementById('answer-reply-form-' + answerId);
    if (replyForm) {
        replyForm.style.display = 'block';
        toggleButton.style.display = 'none';
    }
}

// 답변 댓글 폼 숨기기 함수
function hideAnswerReplyForm(answerId) {
    const replyForm = document.getElementById('answer-reply-form-' + answerId);
    if (replyForm) {
        const answerContainer = replyForm.closest('.answer-post-container');
        const toggleButton = answerContainer ? answerContainer.querySelector('.answer-reply-toggle') : null;

        replyForm.style.display = 'none';
        if (toggleButton) {
            toggleButton.style.display = 'inline-block';
        }

        // 폼 내용 초기화
        const textarea = replyForm.querySelector('textarea');
        if (textarea) {
            textarea.value = '';
        }
    }
}

// 외부 클릭 시 메뉴 닫기
document.addEventListener('click', function(event) {
    if (!event.target.closest('.more-menu-container') &&
        !event.target.closest('.comment-more') &&
        !event.target.closest('.reply-more')) {
        const allMenus = document.querySelectorAll('.more-menu, .comment-more-menu');
        allMenus.forEach(menu => {
            menu.style.display = 'none';
        });
    }
});

// 페이지 로드 시 스크롤 이벤트 추가 (메뉴 닫기)
window.addEventListener('scroll', function() {
    const allMenus = document.querySelectorAll('.more-menu, .comment-more-menu');
    allMenus.forEach(menu => {
        menu.style.display = 'none';
    });
});

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 신고 버튼 이벤트 리스너 추가
    const reportButton = document.getElementById('report-button');
    if (reportButton) {
        reportButton.addEventListener('click', reportPost);
    }

  (function loadPostContent(){
    if (POST_TYPE !== 'AIPLAY') return;

    const postContentBoxes = document.querySelectorAll('.post-content-box');

    for (let box of postContentBoxes){
        box.innerHTML = marked.parse(box.innerHTML);
    }
  })();
});