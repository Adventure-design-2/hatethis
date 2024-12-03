package com.example.hatethis.data

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseService {
    private val storageReference: StorageReference by lazy {
        FirebaseStorage.getInstance().reference
    }

    /**
     * 사진 업로드
     * @param fileUri 사진의 Uri
     * @param folderPath Firebase Storage에 저장할 폴더 경로
     * @return 업로드된 사진의 다운로드 URL
     */
    suspend fun uploadPhoto(fileUri: Uri, folderPath: String): String {
        require(folderPath.isNotBlank()) { "폴더 경로는 비어 있을 수 없습니다." }

        try {
            // 고유 파일 이름 생성
            val fileName = UUID.randomUUID().toString()
            val fileReference = storageReference.child("$folderPath/$fileName")

            // Firebase Storage에 파일 업로드
            fileReference.putFile(fileUri).await()

            // 업로드된 파일의 다운로드 URL 가져오기
            return fileReference.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw IllegalStateException("사진 업로드 실패: ${e.message}", e)
        }
    }

    /**
     * 사진 삭제
     * @param photoUrl 삭제할 사진의 다운로드 URL
     */
    suspend fun deletePhoto(photoUrl: String) {
        try {
            // Firebase Storage 경로에서 파일 삭제
            val fileReference = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl)
            fileReference.delete().await()
        } catch (e: Exception) {
            throw IllegalStateException("사진 삭제 실패: ${e.message}", e)
        }
    }
}
