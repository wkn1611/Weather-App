package com.mi3mien.weatherapp

// Hàm chuẩn hóa tên thành phố trước khi gọi API
fun normalizeCityName(cityName: String): String {
    val vietnameseMap = mapOf(
        'à' to 'a', 'á' to 'a', 'ả' to 'a', 'ã' to 'a', 'ạ' to 'a',
        'â' to 'a', 'ầ' to 'a', 'ấ' to 'a', 'ẩ' to 'a', 'ẫ' to 'a', 'ậ' to 'a',
        'ă' to 'a', 'ằ' to 'a', 'ắ' to 'a', 'ẳ' to 'a', 'ẵ' to 'a', 'ặ' to 'a',
        'è' to 'e', 'é' to 'e', 'ẻ' to 'e', 'ẽ' to 'e', 'ẹ' to 'e',
        'ê' to 'e', 'ề' to 'e', 'ế' to 'e', 'ể' to 'e', 'ễ' to 'e', 'ệ' to 'e',
        'ì' to 'i', 'í' to 'i', 'ỉ' to 'i', 'ĩ' to 'i', 'ị' to 'i',
        'ò' to 'o', 'ó' to 'o', 'ỏ' to 'o', 'õ' to 'o', 'ọ' to 'o',
        'ô' to 'o', 'ồ' to 'o', 'ố' to 'o', 'ổ' to 'o', 'ỗ' to 'o', 'ộ' to 'o',
        'ơ' to 'o', 'ờ' to 'o', 'ớ' to 'o', 'ở' to 'o', 'ỡ' to 'o', 'ợ' to 'o',
        'ù' to 'u', 'ú' to 'u', 'ủ' to 'u', 'ũ' to 'u', 'ụ' to 'u',
        'ư' to 'u', 'ừ' to 'u', 'ứ' to 'u', 'ử' to 'u', 'ữ' to 'u', 'ự' to 'u',
        'ỳ' to 'y', 'ý' to 'y', 'ỷ' to 'y', 'ỹ' to 'y', 'ỵ' to 'y',
        'đ' to 'd',
        'À' to 'A', 'Á' to 'A', 'Ả' to 'A', 'Ã' to 'A', 'Ạ' to 'A',
        'Â' to 'A', 'Ầ' to 'A', 'Ấ' to 'A', 'Ẩ' to 'A', 'Ẫ' to 'A', 'Ậ' to 'A',
        'Ă' to 'A', 'Ằ' to 'A', 'Ắ' to 'A', 'Ẳ' to 'A', 'Ẵ' to 'A', 'Ặ' to 'A',
        'È' to 'E', 'É' to 'E', 'Ẻ' to 'E', 'Ẽ' to 'E', 'Ẹ' to 'E',
        'Ê' to 'E', 'Ề' to 'E', 'Ế' to 'E', 'Ể' to 'E', 'Ễ' to 'E', 'Ệ' to 'E',
        'Ì' to 'I', 'Í' to 'I', 'Ỉ' to 'I', 'Ĩ' to 'I', 'Ị' to 'I',
        'Ò' to 'O', 'Ó' to 'O', 'Ỏ' to 'O', 'Õ' to 'O', 'Ọ' to 'O',
        'Ô' to 'O', 'Ồ' to 'O', 'Ố' to 'O', 'Ổ' to 'O', 'Ỗ' to 'O', 'Ộ' to 'O',
        'Ơ' to 'O', 'Ờ' to 'O', 'Ớ' to 'O', 'Ở' to 'O', 'Ỡ' to 'O', 'Ợ' to 'O',
        'Ù' to 'U', 'Ú' to 'U', 'Ủ' to 'U', 'Ũ' to 'U', 'Ụ' to 'U',
        'Ư' to 'U', 'Ừ' to 'U', 'Ứ' to 'U', 'Ử' to 'U', 'Ữ' to 'U', 'Ự' to 'U',
        'Ỳ' to 'Y', 'Ý' to 'Y', 'Ỷ' to 'Y', 'Ỹ' to 'Y', 'Ỵ' to 'Y',
        'Đ' to 'D'
    )

    return cityName.map { vietnameseMap[it] ?: it }.joinToString("").trim()
}