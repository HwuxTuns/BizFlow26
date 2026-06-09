/**
 * Vietnamese Address Database for Ho Chi Minh City
 * Contains common addresses with street numbers, names, wards, and districts
 * Used for autocomplete/suggestion functionality
 */

const VIETNAMESE_ADDRESSES_HCM = [
  // District 1 - Central Saigon
  "1 Nguyễn Huệ, Phường Bến Nghé, Quận 1, Thành phố Hồ Chí Minh",
  "2 Lê Thánh Tôn, Phường Bến Nghé, Quận 1, Thành phố Hồ Chí Minh",
  "5 Ngô Đức Kế, Phường Bến Nghé, Quận 1, Thành phố Hồ Chí Minh",
  "10 Phạm Ngũ Lão, Phường Đa Kao, Quận 1, Thành phố Hồ Chí Minh",
  "15 Pasteur, Phường Đa Kao, Quận 1, Thành phố Hồ Chí Minh",
  "25 Thái Văn Lung, Phường Bến Nghé, Quận 1, Thành phố Hồ Chí Minh",
  "35 Tháp Mười, Phường Đa Kao, Quận 1, Thành phố Hồ Chí Minh",
  "42 Tôn Thất Đạm, Phường Bến Nghé, Quận 1, Thành phố Hồ Chí Minh",
  "50 Lý Tự Trọng, Phường Bến Nghé, Quận 1, Thành phố Hồ Chí Minh",
  "60 Đinh Tiên Hoàng, Phường Bến Nghé, Quận 1, Thành phố Hồ Chí Minh",
  "72 Hàng Trống, Phường Tân Định, Quận 1, Thành phố Hồ Chí Minh",
  "85 Ô Cơm, Phường Bến Nghé, Quận 1, Thành phố Hồ Chí Minh",
  "95 Hàng Vải, Phường Tân Định, Quận 1, Thành phố Hồ Chí Minh",
  "135 Calmette, Phường Bến Nghé, Quận 1, Thành phố Hồ Chí Minh",
  "145 Tôn Tất Đạm, Phường Bến Nghé, Quận 1, Thành phố Hồ Chí Minh",
  "165 Hai Bà Trưng, Phường Bến Nghé, Quận 1, Thành phố Hồ Chí Minh",
  "185 Nguyễn Huệ, Phường Bến Nghé, Quận 1, Thành phố Hồ Chí Minh",

  // District 3 - Cosmopolitan Area
  "60 Nguyễn Công Trứ, Phường 5, Quận 3, Thành phố Hồ Chí Minh",
  "65 Điện Biên Phủ, Phường 2, Quận 3, Thành phố Hồ Chí Minh",
  "75 Võ Văn Tần, Phường 6, Quận 3, Thành phố Hồ Chí Minh",
  "105 Cao Thắng, Phường 5, Quận 3, Thành phố Hồ Chí Minh",
  "125 Nguyễn Đình Chiểu, Phường 2, Quận 3, Thành phố Hồ Chí Minh",
  "178 Cách Mạng Tháng 8, Phường 9, Quận 3, Thành phố Hồ Chí Minh",
  "200 Cách Mạng Tháng 8, Phường 10, Quận 3, Thành phố Hồ Chí Minh",
  "245 Nguyễn Đình Chiểu, Phường 5, Quận 3, Thành phố Hồ Chí Minh",

  // District 4
  "100 Võ Văn Kiệt, Phường 14, Quận 4, Thành phố Hồ Chí Minh",
  "250 Hoàng Sa, Phường 6, Quận 4, Thành phố Hồ Chí Minh",

  // District 5 - Cholon
  "25 Mạc Thiên Tích, Phường 8, Quận 5, Thành phố Hồ Chí Minh",
  "50 Nguyễn Trãi, Phường 8, Quận 5, Thành phố Hồ Chí Minh",
  "55 Tạo Đàn, Phường 7, Quận 5, Thành phố Hồ Chí Minh",
  "150 Nguyễn Trãi, Phường 7, Quận 5, Thành phố Hồ Chí Minh",
  "175 Mạc Thiên Tích, Phường 7, Quận 5, Thành phố Hồ Chí Minh",

  // District 6
  "85 Tạo Đàn, Phường 12, Quận 6, Thành phố Hồ Chí Minh",
  "120 Trần Văn Đa, Phường 14, Quận 6, Thành phố Hồ Chí Minh",

  // District 8
  "50 Phạm Hùng, Phường 9, Quận 8, Thành phố Hồ Chí Minh",
  "100 Tạ Quang Bửu, Phường 4, Quận 8, Thành phố Hồ Chí Minh",

  // District 10
  "75 Hòa Hư, Phường 9, Quận 10, Thành phố Hồ Chí Minh",
  "155 Sư Vạn Hạnh, Phường 13, Quận 10, Thành phố Hồ Chí Minh",
  "200 Cộng Hòa, Phường 12, Quận 10, Thành phố Hồ Chí Minh",

  // District 11
  "30 Lạc Long Quân, Phường 3, Quận 11, Thành phố Hồ Chí Minh",
  "120 Thạch Lam, Phường 5, Quận 11, Thành phố Hồ Chí Minh",

  // District 12
  "45 Nguyễn Hữu Thọ, Phường 6, Quận 12, Thành phố Hồ Chí Minh",
  "150 Tây Thạnh, Phường 6, Quận 12, Thành phố Hồ Chí Minh",

  // Tân Phú District - User Requested Addresses
  "25 Gò Dầu, Phường Tân Quý, Quận Tân Phú, Thành phố Hồ Chí Minh",
  "70 Tô Ký, Phường Tân Chánh Hiệp, Quận Tân Phú, Thành phố Hồ Chí Minh",
  "100 Gò Dầu, Phường Tân Quý, Quận Tân Phú, Thành phố Hồ Chí Minh",
  "100 Lũy Bán Bích, Phường Tân Thành, Quận Tân Phú, Thành phố Hồ Chí Minh",
  "150 Tô Ký, Phường Tân Chánh Hiệp, Quận Tân Phú, Thành phố Hồ Chí Minh",
  "160 Tô Ký, Phường Tân Chánh Hiệp, Quận Tân Phú, Thành phố Hồ Chí Minh",
  "178 Gò Dầu, Phường Tân Quý, Quận Tân Phú, Thành phố Hồ Chí Minh",
  "180 Gò Dầu, Phường Tân Quý, Quận Tân Phú, Thành phố Hồ Chí Minh",
  "200 Gò Dầu, Phường Tân Quý, Quận Tân Phú, Thành phố Hồ Chí Minh",
  "50 Lũy Bán Bích, Phường Tân Thành, Quận Tân Phú, Thành phố Hồ Chí Minh",

  // Bình Tân District
  "88 Lý Thường Kiệt, Phường 11, Quận Bình Tân, Thành phố Hồ Chí Minh",
  "120 Quốc Lộ 1A, Phường Bình Hưng Hòa, Quận Bình Tân, Thành phố Hồ Chí Minh",

  // Phú Nhuận District
  "50 Lê Văn Sỹ, Phường 12, Quận Phú Nhuận, Thành phố Hồ Chí Minh",
  "100 3 Tháng 2, Phường 11, Quận Phú Nhuận, Thành phố Hồ Chí Minh",
  "200 Quang Trung, Phường 11, Quận Phú Nhuận, Thành phố Hồ Chí Minh",

  // Thủ Đức District - New Urban Area
  "45 Đường D1, Phường Thảo Điền, Quận Thủ Đức, Thành phố Hồ Chí Minh",
  "100 Đường D2, Phường Thảo Điền, Quận Thủ Đức, Thành phố Hồ Chí Minh",
  "200 Đường D5, Phường Thảo Điền, Quận Thủ Đức, Thành phố Hồ Chí Minh",

  // Gò Vấp District
  "50 Phan Văn Trị, Phường 5, Quận Gò Vấp, Thành phố Hồ Chí Minh",
  "100 Yersin, Phường 15, Quận Gò Vấp, Thành phố Hồ Chí Minh",

  // Bình Thạnh District
  "10 Điện Biên Phủ, Phường 25, Quận Bình Thạnh, Thành phố Hồ Chí Minh",
  "30 Lê Thị Riêng, Phường 12, Quận Bình Thạnh, Thành phố Hồ Chí Minh",
  "80 Lý Chính Thắng, Phường 9, Quận Bình Thạnh, Thành phố Hồ Chí Minh",
  "120 Nguyễn Thị Minh Khai, Phường 5, Quận Bình Thạnh, Thành phố Hồ Chí Minh",

  // Tân Bình District
  "115 Trường Chinh, Phường 12, Quận Tân Bình, Thành phố Hồ Chí Minh",

  // Additional High-Traffic Areas
  "40 Phan Đình Phùng, Phường 5, Quận 3, Thành phố Hồ Chí Minh",
  "45 Hoàng Dieu, Phường Đa Kao, Quận 1, Thành phố Hồ Chí Minh",
];

/**
 * Search Vietnamese addresses by query string
 * Performs precise matching on street number, street name, ward, district
 * Prioritizes street number matches for accuracy
 * @param {string} query - Search query (e.g., "178", "70", "gò dầu", "tô ký")
 * @returns {Array<string>} - Array of matching addresses sorted by relevance
 */
function searchVietnameseAddresses(query) {
  if (!query || query.trim().length < 1) {
    return [];
  }

  const trimmedQuery = query.trim();
  const matches = [];
  const exactMatches = [];
  const streetNumberMatches = [];
  const partialMatches = [];
  const matchedAddresses = new Set();

  // Remove accents for better matching
  const normalizeForSearch = (str) => {
    return str
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim();
  };

  const normalizedQuery = normalizeForSearch(trimmedQuery);
  const isNumericQuery = /^\d+$/.test(trimmedQuery);

  VIETNAMESE_ADDRESSES_HCM.forEach((address) => {
    if (matchedAddresses.has(address)) {
      return;
    }

    const normalizedAddress = normalizeForSearch(address);

    // 1. EXACT SUBSTRING MATCH (highest priority)
    if (normalizedAddress.includes(normalizedQuery)) {
      exactMatches.push(address);
      matchedAddresses.add(address);
      return;
    }

    // 2. STREET NUMBER MATCH (for numeric queries)
    if (isNumericQuery) {
      // Extract street number from address (first part before space)
      const addressParts = normalizedAddress.split(/\s+/);
      const streetNumber = addressParts[0];

      // Exact street number match
      if (streetNumber === normalizedQuery) {
        streetNumberMatches.push(address);
        matchedAddresses.add(address);
        return;
      }

      // Street number starts with query (e.g., "17" matches "178")
      if (streetNumber.startsWith(normalizedQuery)) {
        streetNumberMatches.push(address);
        matchedAddresses.add(address);
        return;
      }
    }

    // 3. WORD-BASED MATCH (for street names, wards, districts)
    const addressParts = normalizedAddress.split(/[,.\s]+/).filter(p => p.length > 0);
    const queryParts = normalizedQuery.split(/[\s,]+/).filter(p => p.length > 0);

    let matchCount = 0;
    for (const queryPart of queryParts) {
      for (const addressPart of addressParts) {
        if (addressPart.includes(queryPart) || queryPart.includes(addressPart)) {
          matchCount++;
          break;
        }
      }
    }

    // Include if at least one query part matches
    if (matchCount > 0) {
      partialMatches.push({ address, score: matchCount / queryParts.length });
      matchedAddresses.add(address);
    }
  });

  // Sort partial matches by relevance score (highest first)
  partialMatches.sort((a, b) => b.score - a.score);

  // Combine results: exact matches → street number matches → partial matches
  const results = [
    ...exactMatches,
    ...streetNumberMatches,
    ...partialMatches.map(m => m.address)
  ];

  return results.slice(0, 8); // Return max 8 results
}

/**
 * Get all available Vietnamese addresses
 * @returns {Array<string>} - All addresses in the database
 */
function getAllVietnameseAddresses() {
  return VIETNAMESE_ADDRESSES_HCM;
}
