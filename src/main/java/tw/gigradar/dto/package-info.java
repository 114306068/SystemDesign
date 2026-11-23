/**
 * API 回傳格式（Java record）。決定哪些欄位可以給外面看，
 * 例如 users.password_hash 不在任何 DTO 裡，所以永遠不會被回傳。
 * JSON 欄位名稱由 spring.jackson.property-naming-strategy 轉成 snake_case（eventDate → event_date）。
 */
package tw.gigradar.dto;
