<template>
  <div class="editor-container">
    <div class="editor-content">
      <div class="preview-section">
        <!-- CA地址 -->
        <div class="preview-ca">
          <label>CA:</label>
          <span class="value clickable" @click="copyToClipboard('0x51e8e215b6e9268ccbeb81cf4cf59ae5a3c14444')" title="点击复制">
            0x51e8e215b6e9268ccbeb81cf4cf59ae5a3c14444
          </span>
        </div>
        <!-- 只显示底图的预览画布 - 增加分辨率 -->
        <canvas ref="previewCanvas" width="900" height="1500" class="preview-canvas"></canvas>
        <!-- 最终生成用的画布 - 增加分辨率 -->
        <canvas ref="finalCanvas" width="900" height="1500" style="display: none;"></canvas>

        <!-- 显示用户输入的预览区 -->
        <div class="preview-text">
          <div class="preview-name">{{ nameText || 'NianNian' }}</div>
          <div class="preview-community">{{ communityText || 'Community' }}</div>
        </div>
      </div>

      <div class="controls-section">
        <div class="controls">
          <el-upload
            class="upload-demo"
            :auto-upload="false"
            accept="image/*"
            :show-file-list="false"
            :on-change="onUpload"
          >
            <el-button type="primary">上传头像</el-button>
          </el-upload>

          <div class="preview-upload" v-if="uploadedImg">
            <img :src="uploadedImg.src" class="preview-image" />
          </div>

          <el-input
            v-model="nameText"
            placeholder="输入昵称"
            class="input-field"
            maxlength="8"
            show-word-limit
          />

          <el-input
            v-model="communityText"
            placeholder="输入社区名"
            class="input-field"
            maxlength="10"
            show-word-limit
          />

          <el-button
            type="primary"
            @click="generateFinal"
          >
            生成并下载
          </el-button>

          <div class="social-info">
            <div class="info-item">
              <span class="label">TG:</span>
              <a href="https://t.me/BNB_ID_CTO" target="_blank" class="value">t.me/BNB_ID_CTO</a>
            </div>
            <div class="info-item">
              <span class="label">X:</span>
              <a href="https://x.com/BNB_ID_CTO" target="_blank" class="value">@BNB_ID_CTO</a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { CopyDocument } from '@element-plus/icons-vue';

const previewCanvas = ref(null);
const finalCanvas = ref(null);
const nameText = ref("");
const communityText = ref("");
const uploadedImg = ref(null);

// 画布尺寸常量
const CANVAS_WIDTH = 900;
const CANVAS_HEIGHT = 1500;

// 头像区域的四个角坐标
const avatarArea = {
  topLeft: {
    x: 315,    // 左上角X坐标   越大往右走
    y: 360     // 左上角Y坐标   越大往下走
  },
  topRight: {
    x: 730,    // 右上角X坐标
    y: 250     // 右上角Y坐标
  },
  bottomLeft: {
    x: 265,    // 左下角X坐标
    y: 760     // 左下角Y坐标
  },
  bottomRight: {
    x: 640,    // 右下角X坐标
    y: 660     // 右下角Y坐标
  }
};

// 加载背景图
const bgImg = new Image();
bgImg.src = "/img_1.png";

// 只绘制底图的预览
const drawPreview = () => {
  const ctx = previewCanvas.value.getContext('2d');
  ctx.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
  
  // 只绘制背景图
  if (bgImg.complete) {
    ctx.drawImage(bgImg, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
  }
};

// 生成最终图片
const generateFinal = () => {
  const ctx = finalCanvas.value.getContext('2d');
  ctx.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
  
  // 绘制背景图
  if (bgImg.complete) {
    ctx.drawImage(bgImg, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
  }

  // 如果有上传的图片，使用四个角坐标来绘制
  if (uploadedImg.value && uploadedImg.value.complete) {
    // 计算绘制区域
    const width = avatarArea.topRight.x - avatarArea.topLeft.x;
    const height = avatarArea.bottomLeft.y - avatarArea.topLeft.y;
    const centerX = avatarArea.topLeft.x + width / 2;
    const centerY = avatarArea.topLeft.y + height / 2;

    // 绘制头像
    ctx.save();

    // 设置旋转中心点
    ctx.translate(centerX, centerY);

    // 向右旋转15度（角度转弧度）
    ctx.rotate(12.5 * Math.PI / 180);

    // 移回原点
    ctx.translate(-centerX, -centerY);

    // 创建裁剪区域
    ctx.beginPath();
    ctx.rect(avatarArea.topLeft.x, avatarArea.topLeft.y, width + 10, height);
    ctx.clip();

    // 绘制图片，使用高质量的图像平滑
    ctx.imageSmoothingEnabled = true;
    ctx.imageSmoothingQuality = 'high';

    ctx.drawImage(
      uploadedImg.value,
      avatarArea.topLeft.x,
      avatarArea.topLeft.y,
      width,
      height
    );
    
    ctx.restore();
  }

  // 绘制文字
  ctx.restore(); // 恢复之前的状态
  
  // 设置文字样式
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  
  // 添加文字倾斜效果
  ctx.save();
  ctx.translate(470, 830); // 移动到昵称的位置
  ctx.rotate(13 * Math.PI / 180); // 倾斜角度
  
  // 绘制昵称
  const truncatedName = (nameText.value || 'NianNian').slice(0, 10);
  // 检查是否包含大写字母
  const hasUpperCase = /[A-Z]/.test(truncatedName);
  ctx.font = `bold ${hasUpperCase ? 60 : 70}px Arial`;
  ctx.fillStyle = '#ba9e6e';
  ctx.fillText(truncatedName, 0, 0);
  
  // 绘制社区名
  ctx.translate(0, 70); // 相对于昵称向下移动
  const truncatedCommunity = (communityText.value || 'Community').slice(0, 15);
  // 检查是否包含大写字母
  const communityHasUpperCase = /[A-Z]/.test(truncatedCommunity);
  ctx.font = `${communityHasUpperCase ? 45 : 52}px Arial`;
  ctx.fillStyle = '#ba9e6e';
  ctx.fillText(truncatedCommunity, 0, 0);
  
  ctx.restore();

  // 下载生成的图片，使用高质量设置
  const link = document.createElement("a");
  link.download = "binancian-card.png";
  link.href = finalCanvas.value.toDataURL("image/png", 1.0);
  link.click();
};

onMounted(() => {
  bgImg.onload = drawPreview;
});

const onUpload = (file) => {
  const reader = new FileReader();
  reader.onload = (event) => {
    const img = new Image();
    img.onload = () => {
      uploadedImg.value = img;
    };
    img.src = event.target.result;
  };
  reader.readAsDataURL(file.raw);
};

// 添加复制函数
const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text);
    ElMessage({
      message: '复制成功',
      type: 'success',
      duration: 2000
    });
  } catch (err) {
    // 如果clipboard API失败，使用传统方法
    const textArea = document.createElement('textarea');
    textArea.value = text;
    document.body.appendChild(textArea);
    textArea.select();
    try {
      document.execCommand('copy');
      ElMessage({
        message: '复制成功',
        type: 'success',
        duration: 2000
      });
    } catch (err) {
      ElMessage({
        message: '复制失败，请手动复制',
        type: 'error',
        duration: 2000
      });
    }
    document.body.removeChild(textArea);
  }
};
</script>

<style scoped>
.editor-container {
  padding: 20px;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #1a1008;
}

.editor-content {
  display: flex;
  gap: 40px;
  align-items: flex-start;
  background: #1a1008;
  padding: 30px;
  border-radius: 15px;
  border: 1px solid #F0B90B;
}

.preview-section {
  flex-shrink: 0;
  position: relative;
}

.preview-canvas {
  width: 300px;
  height: 500px;
  border-radius: 15px;
  display: block;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.preview-text {
  position: absolute;
  right: -260px;
  top: 56%;
  transform: translateY(-50%);
  text-align: center;
  background: #1a1008;
  padding: 20px;
  border-radius: 8px;
  width: 200px;
  border: 1px solid #F0B90B;
}

.preview-name {
  font-size: 24px;
  font-weight: bold;
  color: #F0B90B;
  margin-bottom: 8px;
}

.preview-community {
  font-size: 18px;
  color: #F0B90B;
}

.controls-section {
  flex-shrink: 0;
  width: 240px;
  padding: 20px;
  border-left: 1px solid #F0B90B;
}

.controls {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}

.input-field {
  width: 100%;
}

:deep(.el-button) {
  width: 100%;
  background-color: #F0B90B !important;
  border-color: #F0B90B !important;
  color: #1a1008 !important;
  font-weight: bold !important;
  height: 40px !important;
  border-radius: 8px !important;
}

:deep(.el-button:hover) {
  opacity: 0.9 !important;
}

:deep(.el-input__wrapper) {
  background: #1a1008 !important;
  box-shadow: 0 0 0 1px #F0B90B inset !important;
  border-radius: 8px !important;
  height: 40px !important;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #F0B90B inset !important;
  opacity: 0.9;
}

:deep(.el-input__inner) {
  color: #F0B90B !important;
  height: 40px !important;
}

:deep(.el-input__inner::placeholder) {
  color: rgba(240, 185, 11, 0.5) !important;
}

.preview-upload {
  width: 100%;
  height: 150px;
  border: 2px dashed #F0B90B;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  margin: 10px 0;
  background: #1a1008;
  transition: all 0.3s ease;
}

.preview-upload:hover {
  opacity: 0.9;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.social-info {
  margin-top: 110PX;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  font-size: 14px;
  width: 100%;
  box-sizing: border-box;
}

.info-item {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #666;
  font-weight: bold;
  width: 40px;
  flex-shrink: 0;
}

.value {
  color: #ba9e6e;
  flex: 1;
}

.value.clickable {
  cursor: pointer;
  font-family: monospace;
  transition: opacity 0.2s;
}

.value.clickable:hover {
  opacity: 0.8;
}

.value a {
  color: #ba9e6e;
  text-decoration: none;
}

.value a:hover {
  text-decoration: underline;
}
</style>
