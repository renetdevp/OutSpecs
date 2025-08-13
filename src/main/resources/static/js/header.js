(() => {
  // 중복 실행 방지
  if (window.__headerDropdownInit) return;
  window.__headerDropdownInit = true;

  const dropdowns = Array.from(document.querySelectorAll('.profile-dropdown'));
  if (!dropdowns.length) return;

  const closeAll = () => dropdowns.forEach(d => {
    d.classList.remove('open');
    const btn = d.querySelector('.avatar-btn');
    if (btn) btn.setAttribute('aria-expanded', 'false');
  });

  const toggle = (dd) => {
    const willOpen = !dd.classList.contains('open');
    closeAll();
    if (willOpen) {
      dd.classList.add('open');
      const btn = dd.querySelector('.avatar-btn');
      if (btn) btn.setAttribute('aria-expanded', 'true');
    }
  };

  // 각 드롭다운에 버튼 이벤트 연결
  dropdowns.forEach(dd => {
    const btn = dd.querySelector('.avatar-btn');
    if (!btn) return;

    // 한 번만 바인딩(새로 삽입되는 경우 대비)
    if (btn.dataset.bound === '1') return;
    btn.dataset.bound = '1';

    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      toggle(dd);
    });
  });

  // 바깥 클릭/ESC로 닫기 (문서 전역은 1회만 바인딩)
  document.addEventListener('click', closeAll);
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') closeAll();
  });
})();