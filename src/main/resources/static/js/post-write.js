  document.addEventListener("DOMContentLoaded", () => {
      // 게시판 타입에 따른 필드 표시/숨김
    const postTypeSelect = document.getElementById("postType");
    const tagsField = document.getElementById("tagsField");
    const qnaAnswerField = document.getElementById("qnaAnswerField");
    const hangoutPlaceField = document.getElementById("hangoutPlaceField");
    const teamStatusField = document.getElementById("teamStatusField");
    const teamCapacityField = document.getElementById("teamCapacityField");
    const jobCareerField = document.getElementById("jobCareerField");
    const jobTechField = document.getElementById("jobTechField");

  function toggleFieldsByPostType() {
      const postType = postTypeSelect.value;

      // 모든 필드 숨김
    [tagsField, qnaAnswerField, hangoutPlaceField, teamStatusField,
     teamCapacityField, jobCareerField, jobTechField].forEach(field => {
      if (field) field.classList.add("hidden");
    });

    // 모든 필드 required 제거
    const allRequiredFields = [
        document.querySelector('[name="hangoutInfo.placeName"]'),
        document.querySelector('[name="teamInfo.capacity"]'),
        document.querySelector('[name="jobInfo.career"]')
    ];
    allRequiredFields.forEach(input => {
        if (input) input.required = false;
    });

    // 게시판 타입에 따라 필드 표시 + required 부여
    switch (postType) {
      case "FREE":
        if (tagsField) tagsField.classList.remove("hidden");
        break;
      case "QNA":
        if (tagsField) tagsField.classList.remove("hidden");
        if (qnaAnswerField) qnaAnswerField.classList.remove("hidden");
        break;
      case "PLAY":
        if (hangoutPlaceField) hangoutPlaceField.classList.remove("hidden");
        const placeInput = document.querySelector('[name="hangoutInfo.placeName"]');
        if (placeInput) placeInput.required = true;
        break;
      case "TEAM":
        if (teamStatusField) teamStatusField.classList.remove("hidden");
        if (teamCapacityField) teamCapacityField.classList.remove("hidden");
        const capInput = document.querySelector('[name="teamInfo.capacity"]');
        if (capInput) capInput.required = true;
        break;
      case "RECRUIT":
        if (jobCareerField) jobCareerField.classList.remove("hidden");
        if (jobTechField) jobTechField.classList.remove("hidden");
        const careerInput = document.querySelector('[name="jobInfo.career"]');
        if (careerInput) careerInput.required = true;
        break;
      }
    }

    postTypeSelect.addEventListener("change", toggleFieldsByPostType);
    toggleFieldsByPostType(); // 초기 로드시 실행

    // 태그 관리 (자유게시판, QnA용)
    const tagCheckboxes = document.querySelectorAll(".tag-chip");
    const tagsHiddenInput = document.getElementById("tagsInput");

    function updateTagsHiddenInput() {
      const selected = Array.from(tagCheckboxes)
        .filter(chk => chk.checked)
        .map(chk => chk.value);
      if (tagsHiddenInput) {
        tagsHiddenInput.value = selected.join(",");
      }
    }

    tagCheckboxes.forEach(chk => {
      chk.addEventListener("change", updateTagsHiddenInput);
    });
    updateTagsHiddenInput();

    // 기술 스택 관리 (채용공고용)
    const techCheckboxes = document.querySelectorAll(".tech-chip");
    const techHiddenInput = document.getElementById("techInput");

    function updateTechHiddenInput() {
      const selected = Array.from(techCheckboxes)
        .filter(chk => chk.checked)
        .map(chk => chk.value);
      if (techHiddenInput) {
        techHiddenInput.value = selected.join(",");
      }
    }

    techCheckboxes.forEach(chk => {
      chk.addEventListener("change", updateTechHiddenInput);
    });
    updateTechHiddenInput();

    // 이미지 업로드 관리
    const imageInput = document.getElementById("images");
    const mainImageBox = document.getElementById("mainImageBox");
    const imageThumbnails = document.getElementById("imageThumbnails");
    const imageCount = document.getElementById("imageCount");

    let selectedFiles = [];
    const maxFiles = 5;

    function updateImageDisplay() {
      imageThumbnails.innerHTML = '';
      imageCount.textContent = selectedFiles.length;

      if (selectedFiles.length > 0) {
        // 첫 번째 이미지를 메인 박스에 표시
        const firstFile = selectedFiles[0];
        const reader = new FileReader();
        reader.onload = (e) => {
          mainImageBox.innerHTML = `<img src="${e.target.result}" alt="Preview">`;
          mainImageBox.classList.add('has-image');
        };
        reader.readAsDataURL(firstFile);

        // 모든 이미지를 썸네일로 표시
        selectedFiles.forEach((file, index) => {
          const reader = new FileReader();
          reader.onload = (e) => {
            const thumbnailItem = document.createElement('div');
            thumbnailItem.className = 'thumbnail-item';
            thumbnailItem.innerHTML = `
                <img src="${e.target.result}" alt="Thumbnail ${index + 1}">
                <button type="button" class="remove-btn" data-index="${index}">&times;</button>
            `;
            imageThumbnails.appendChild(thumbnailItem);
            };
            reader.readAsDataURL(file);
          });
        } else {
          // 이미지가 없을 때 기본 상태로 복원
          mainImageBox.innerHTML = `
            <div class="upload-text">
                <div class="upload-icon">📷</div>
                <span>이미지 추가</span>
                <small>(최대 5개)</small>
            </div>
          `;
          mainImageBox.classList.remove('has-image');
        }

        // 파일 input 업데이트
        updateFileInput();
    }

    function updateFileInput() {
        const dataTransfer = new DataTransfer();
        selectedFiles.forEach(file => {
            dataTransfer.items.add(file);
        });
        imageInput.files = dataTransfer.files;
    }

    imageInput.addEventListener('change', (e) => {
        const files = Array.from(e.target.files);

        // 파일이 선택되지 않았으면 리턴
        if (files.length === 0) return;

        if (selectedFiles.length + files.length > maxFiles) {
            alert(`최대 ${maxFiles}개의 이미지만 업로드할 수 있습니다.`);
            // input을 초기화하여 선택된 파일을 제거
            imageInput.value = '';
            return;
        }

        // 파일 크기 검증 (5MB 제한)
        const maxSize = 5 * 1024 * 1024; // 5MB
        const invalidFiles = files.filter(file => file.size > maxSize);
        if (invalidFiles.length > 0) {
            alert('파일 크기는 5MB를 초과할 수 없습니다.');
            // input을 초기화하여 선택된 파일을 제거
            imageInput.value = '';
            return;
        }

        selectedFiles = [...selectedFiles, ...files];
        updateImageDisplay();
    });

    // 썸네일 삭제 버튼 이벤트
    imageThumbnails.addEventListener('click', (e) => {
        if (e.target.classList.contains('remove-btn')) {
            const index = parseInt(e.target.dataset.index);
            selectedFiles.splice(index, 1);
            updateImageDisplay();
        }
    });

    // 최대 파일 개수에 도달했을 때 파일 선택 방지
    mainImageBox.addEventListener('click', (e) => {
        if (selectedFiles.length >= maxFiles) {
            e.preventDefault();
            alert(`최대 ${maxFiles}개의 이미지만 업로드할 수 있습니다.`);
        }
    });
});