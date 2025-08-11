(function(){
  const form   = document.querySelector('.op-form');
  const hidden = document.getElementById('stacksInput');
  const chips  = Array.from(document.querySelectorAll('input.stack-chip[name="stackChips"]'));
  const errEl  = document.getElementById('stacksClientError');
  const fileInput  = document.getElementById('file');
  const avatarPrev = document.getElementById('avatarPreview');
  const removeBtn = document.getElementById('removeImageBtn');
  const cropModal = document.getElementById('cropModal');
  const cropCanvas = document.getElementById('cropCanvas');
  const cropConfirm = document.getElementById('cropConfirm');
  const cropCancel = document.getElementById('cropCancel');

  if (!form) return;

  let originalImageSrc = null;
  let currentFile = null;
  let cropContext = null;
  let originalImage = null;

  // 초기 이미지 상태 확인
  checkImageState();

  // 파일 선택 시 처리
  if (fileInput && avatarPrev) {
    fileInput.addEventListener('change', function(){
      const file = this.files && this.files[0];
      if (!file) return;

      // 파일 크기 체크 (5MB)
      if (file.size > 5 * 1024 * 1024) {
        alert('파일 크기는 5MB 이하로 선택해주세요.');
        this.value = '';
        return;
      }

      // 이미지 파일 체크
      if (!file.type.startsWith('image/')) {
        alert('이미지 파일만 업로드 가능합니다.');
        this.value = '';
        return;
      }

      currentFile = file;
      showCropModal(file);
    });
  }

  // 이미지 삭제 버튼
  if (removeBtn) {
    removeBtn.addEventListener('click', function(e) {
      e.preventDefault();
      e.stopPropagation();
      removeImage();
    });
  }

  // 크롭 모달 관련
  if (cropConfirm) {
    cropConfirm.addEventListener('click', applyCrop);
  }

  if (cropCancel) {
    cropCancel.addEventListener('click', closeCropModal);
  }

  function checkImageState() {
    if (!avatarPrev || !removeBtn) return;

    const currentSrc = avatarPrev.src;
    const isDefaultImage = currentSrc.includes('profile_add_icon.svg');

    if (isDefaultImage) {
      removeBtn.style.display = 'none';
      originalImageSrc = currentSrc;
    } else {
      removeBtn.style.display = 'flex';
      originalImageSrc = currentSrc;
    }
  }

  function removeImage() {
    if (avatarPrev && removeBtn) {
      avatarPrev.src = '/css/images/profile_add_icon.svg';
      removeBtn.style.display = 'none';

      // 파일 입력 초기화
      if (fileInput) {
        fileInput.value = '';
      }
      currentFile = null;
    }
  }

  function showCropModal(file) {
    if (!cropModal || !cropCanvas) return;

    const reader = new FileReader();
    reader.onload = function(e) {
      const img = new Image();
      img.onload = function() {
        originalImage = img;
        setupCanvas(img);
        cropModal.style.display = 'flex';
      };
      img.src = e.target.result;
    };
    reader.readAsDataURL(file);
  }

  let cropArea = { x: 0, y: 0, size: 0 };
  let scale = 1;

  function setupCanvas(img) {
    const canvas = cropCanvas;
    const ctx = canvas.getContext('2d');
    cropContext = ctx;

    // 캔버스 크기 설정
    const maxDisplaySize = 400;
    const containerSize = Math.min(maxDisplaySize, Math.max(img.width, img.height));
    canvas.width = containerSize;
    canvas.height = containerSize;

    // 스케일 계산
    scale = containerSize / Math.max(img.width, img.height);
    const scaledWidth = img.width * scale;
    const scaledHeight = img.height * scale;

    // 크롭 영역 초기화 (정사각형, 중앙)
    const cropSize = Math.min(scaledWidth, scaledHeight) * 0.8;
    cropArea = {
      x: (containerSize - cropSize) / 2,
      y: (containerSize - cropSize) / 2,
      size: cropSize
    };

    drawCanvas(img);
    setupCanvasEvents(canvas);
  }

  function drawCanvas(img) {
    const canvas = cropCanvas;
    const ctx = cropContext;

    // 캔버스 클리어
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // 배경 이미지 그리기
    const scaledWidth = img.width * scale;
    const scaledHeight = img.height * scale;
    const imgX = (canvas.width - scaledWidth) / 2;
    const imgY = (canvas.height - scaledHeight) / 2;

    ctx.drawImage(img, imgX, imgY, scaledWidth, scaledHeight);

    // 어두운 오버레이
    ctx.fillStyle = 'rgba(0, 0, 0, 0.5)';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // 크롭 영역 클리어 (밝게)
    ctx.clearRect(cropArea.x, cropArea.y, cropArea.size, cropArea.size);

    // 크롭 영역에 이미지 다시 그리기
    ctx.drawImage(img, imgX, imgY, scaledWidth, scaledHeight);

    // 크롭 경계선
    ctx.strokeStyle = '#2f3096';
    ctx.lineWidth = 3;
    ctx.strokeRect(cropArea.x, cropArea.y, cropArea.size, cropArea.size);

    // 가이드 텍스트
    ctx.fillStyle = '#2f3096';
    ctx.font = '14px sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText('드래그로 이동, 휠로 크기 조정', canvas.width / 2, canvas.height - 15);
  }

  function setupCanvasEvents(canvas) {
    let isMouseDown = false;
    let lastMousePos = { x: 0, y: 0 };

    canvas.addEventListener('mousedown', function(e) {
      const rect = canvas.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;

      // 크롭 영역 내부에서만 드래그 시작
      if (x >= cropArea.x && x <= cropArea.x + cropArea.size &&
          y >= cropArea.y && y <= cropArea.y + cropArea.size) {
        isMouseDown = true;
        lastMousePos = { x, y };
        canvas.style.cursor = 'move';
        e.preventDefault();
      }
    });

    canvas.addEventListener('mousemove', function(e) {
      const rect = canvas.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;

      if (isMouseDown) {
        // 드래그로 크롭 영역 이동
        const dx = x - lastMousePos.x;
        const dy = y - lastMousePos.y;

        const newX = Math.max(0, Math.min(canvas.width - cropArea.size, cropArea.x + dx));
        const newY = Math.max(0, Math.min(canvas.height - cropArea.size, cropArea.y + dy));

        cropArea.x = newX;
        cropArea.y = newY;

        lastMousePos = { x, y };
        drawCanvas(originalImage);
      } else {
        // 마우스 커서 변경
        if (x >= cropArea.x && x <= cropArea.x + cropArea.size &&
            y >= cropArea.y && y <= cropArea.y + cropArea.size) {
          canvas.style.cursor = 'move';
        } else {
          canvas.style.cursor = 'default';
        }
      }
    });

    canvas.addEventListener('mouseup', function() {
      isMouseDown = false;
      canvas.style.cursor = 'default';
    });

    canvas.addEventListener('mouseleave', function() {
      isMouseDown = false;
      canvas.style.cursor = 'default';
    });

    // 휠로 크롭 크기 조정
    canvas.addEventListener('wheel', function(e) {
      e.preventDefault();

      const rect = canvas.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;

      // 크롭 영역 위에서만 크기 조정
      if (x >= cropArea.x && x <= cropArea.x + cropArea.size &&
          y >= cropArea.y && y <= cropArea.y + cropArea.size) {

        const delta = e.deltaY > 0 ? -10 : 10;
        const newSize = Math.max(50, Math.min(
          Math.min(canvas.width, canvas.height) * 0.9,
          cropArea.size + delta
        ));

        // 중심점을 유지하면서 크기 조정
        const centerX = cropArea.x + cropArea.size / 2;
        const centerY = cropArea.y + cropArea.size / 2;

        cropArea.size = newSize;
        cropArea.x = Math.max(0, Math.min(canvas.width - newSize, centerX - newSize / 2));
        cropArea.y = Math.max(0, Math.min(canvas.height - newSize, centerY - newSize / 2));

        drawCanvas(originalImage);
      }
    });
  }

  function applyCrop() {
    if (!cropCanvas || !avatarPrev || !originalImage) return;

    // 최종 캔버스 생성 (120x120)
    const finalCanvas = document.createElement('canvas');
    const finalCtx = finalCanvas.getContext('2d');
    finalCanvas.width = 120;
    finalCanvas.height = 120;

    // 원본 이미지 좌표로 변환
    const scaledWidth = originalImage.width * scale;
    const scaledHeight = originalImage.height * scale;
    const imgX = (cropCanvas.width - scaledWidth) / 2;
    const imgY = (cropCanvas.height - scaledHeight) / 2;

    // 크롭 영역을 원본 이미지 좌표로 변환
    const sourceX = (cropArea.x - imgX) / scale;
    const sourceY = (cropArea.y - imgY) / scale;
    const sourceSize = cropArea.size / scale;

    // 원본 이미지에서 크롭 영역을 120x120으로 그리기
    finalCtx.drawImage(
      originalImage,
      sourceX, sourceY, sourceSize, sourceSize,  // 소스 영역
      0, 0, 120, 120                             // 대상 영역 (120x120)
    );

    // 결과를 아바타에 설정
    const dataURL = finalCanvas.toDataURL('image/jpeg', 0.8);
    avatarPrev.src = dataURL;

    // 삭제 버튼 표시
    if (removeBtn) {
      removeBtn.style.display = 'flex';
    }

    // 파일을 blob으로 변환하여 form에 설정
    finalCanvas.toBlob(function(blob) {
      if (blob && fileInput) {
        // File 객체로 변환
        const resizedFile = new File([blob], currentFile.name, {
          type: 'image/jpeg',
          lastModified: Date.now()
        });

        // FileList 생성 (브라우저 호환성을 위해)
        const dt = new DataTransfer();
        dt.items.add(resizedFile);
        fileInput.files = dt.files;
      }
    }, 'image/jpeg', 0.8);

    closeCropModal();
  }

  function closeCropModal() {
    if (cropModal) {
      cropModal.style.display = 'none';
    }
    // 파일이 선택되지 않았다면 input 초기화
    if (!avatarPrev.src.startsWith('data:') && avatarPrev.src.includes('profile_add_icon.svg')) {
      if (fileInput) {
        fileInput.value = '';
      }
      currentFile = null;
    }
  }

  // 모달 배경 클릭 시 닫기
  if (cropModal) {
    cropModal.addEventListener('click', function(e) {
      if (e.target === cropModal) {
        closeCropModal();
      }
    });
  }

  // stacks 초기 복원
  if (hidden && hidden.value) {
    const set = new Set(hidden.value.split(',').map(s => s.trim()).filter(Boolean));
    chips.forEach(c => { if (set.has(c.value)) c.checked = true; });
  }
  syncHidden(); validateStacks();

  // 변경 시 동기화 + 에러 숨김/표시
  chips.forEach(c => c.addEventListener('change', () => { syncHidden(); validateStacks(); }));

  // 제출 전 마지막 검증
  form.addEventListener('submit', function(e){
    syncHidden(); validateStacks();
    if (!form.checkValidity()) e.preventDefault();
  });

  function syncHidden(){
    if (!hidden) return;
    hidden.value = chips.filter(c => c.checked).map(c => c.value).join(',');
  }

  function validateStacks(){
    const any = chips.some(c => c.checked);
    const target = chips[0];
    if (!target) return;
    if (any) {
      target.setCustomValidity('');
      if (errEl) { errEl.style.display='none'; errEl.textContent=''; }
    } else {
      target.setCustomValidity('기술 스택은 최소 1개 이상 선택해야 합니다.');
      if (errEl) {
        errEl.textContent = '기술 스택은 최소 1개 이상 선택해야 합니다.';
        errEl.style.display = 'block';
      }
    }
  }
})();