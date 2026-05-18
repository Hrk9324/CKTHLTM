# Phân công giám thị và giám sát

## 1. Mô tả bài toán

Dự án xây dựng hệ thống tự động phân công cán bộ coi thi và cán bộ giám sát hành lang cho các ca thi.

Dữ liệu gốc được nhập vào database gồm:

```text
can_bo      : danh sách cán bộ
phong_thi   : danh sách phòng thi
yeu_cau     : lịch sử các yêu cầu tạo ca thi
giam_sat    : lịch sử cán bộ giám sát phòng thi
```

Mỗi lần người dùng gửi yêu cầu:

```text
n = số phòng thi cần dùng cho ca thi hiện tại
m = tổng số cán bộ cần xuất hiện trong kết quả ca thi hiện tại
```

Hệ thống phải tạo ra phân công cho một ca thi mới, thỏa mãn:

```text
1. Có đúng n phòng thi được sử dụng.
2. Có đúng m cán bộ được sử dụng.
3. Mỗi phòng thi có đúng 2 giám thị khác nhau.
4. Một cán bộ không được coi lại phòng thi mà mình đã từng coi ở ca thi khác.
5. Hai cán bộ đã từng coi chung một phòng thì không được coi chung lại ở ca thi khác.
6. Cán bộ giám sát không được giám sát lại phòng mình đã từng giám sát.
7. Cán bộ từng giám sát một phòng vẫn có thể coi thi phòng đó, miễn là chưa từng coi thi phòng đó.
8. Mỗi request từ người dùng được xem là một ca thi khác.
```

Kết quả được xuất ra một file Excel gồm hai sheet:

```text
Sheet 1: PhanCong
Sheet 2: GiamSat
```

---

## 2. Ý nghĩa các tham số

### 2.1. Dữ liệu trong database

Gọi:

```text
N = tổng số phòng thi trong database
M = tổng số cán bộ trong database
```

Ví dụ:

```text
N = 100 phòng thi
M = 300 cán bộ
```

### 2.2. Dữ liệu trong request

Mỗi request có dạng:

```text
n=20, m=60
```

Trong đó:

```text
n = số phòng thi cần lấy cho ca thi hiện tại
m = tổng số cán bộ phải xuất hiện trong kết quả ca thi hiện tại
```

Vì mỗi phòng cần 2 giám thị:

```text
số cán bộ làm giám thị = 2n
```

Số cán bộ giám sát cần dùng là:

```text
số cán bộ giám sát = m - 2n
```

Ví dụ:

```text
n = 20
m = 60

Số cán bộ giám thị = 2 * 20 = 40
Số cán bộ giám sát = 60 - 40 = 20
Tổng cán bộ dùng = 40 + 20 = 60
```

---

## 3. Điều kiện đầu vào

Trước khi tạo phân công, cần kiểm tra:

```text
n > 0
m > 0
n <= N
m <= M
m >= 2n
```

Trong code:

```java
if (n <= 0 || m <= 0) {
    throw new Exception("n va m phai lon hon 0");
}

if (n > phongThiFullList.size()) {
    throw new Exception("So phong thi yeu cau lon hon so phong thi trong database");
}

if (m > canBoFullList.size()) {
    throw new Exception("So can bo yeu cau lon hon so can bo trong database");
}

if (m < 2 * n) {
    throw new Exception("Khong du can bo: moi phong can 2 giam thi");
}
```

---

## 4. Ý tưởng thuật toán

Thuật toán không sinh lịch chỉ dựa trên `n` phòng và `m` cán bộ của từng request.

Thay vào đó, thuật toán sinh lịch trên toàn bộ không gian dữ liệu trong database:

```text
Toàn bộ N phòng thi
Toàn bộ M cán bộ
```

Sau đó mỗi request chỉ lấy một đoạn nhỏ trong lịch đã sinh.

Ví dụ database có:

```text
N = 100 phòng
M = 300 cán bộ
```

Request đầu tiên:

```text
n = 20
m = 60
```

Hệ thống lấy 20 phân công đầu tiên của line 0:

```text
line = 0
offset = 0
lấy phòng index 0 đến 19
```

Request tiếp theo cũng cần 20 phòng:

```text
line = 0
offset = 20
lấy phòng index 20 đến 39
```

Khi line hiện tại không còn đủ phòng thì chuyển sang line tiếp theo:

```text
line = line + 1
offset = 0
```

---

## 5. Cách đánh số dữ liệu

Trước khi sinh lịch, danh sách cán bộ và phòng thi phải được sort theo `id`.

```sql
SELECT * FROM can_bo ORDER BY id
SELECT * FROM phong_thi ORDER BY id
```

Sau khi sort, ta xem:

```text
Phòng thi: P0, P1, P2, ..., P(N - 1)
Cán bộ  : C0, C1, C2, ..., C(M - 1)
```

Lưu ý:

```text
Index 0, 1, 2, ... chỉ là chỉ số nội bộ của thuật toán.
maGV là mã cán bộ thật trong database.
```

## 6. Cách chọn S

Thuật toán chia cán bộ thành hai nhóm chính để làm giám thị:

```text
Nhóm A: C0, C1, ..., C(S - 1)
Nhóm B: CS, C(S + 1), ..., C(2S - 1)
```

Hàm tính `S`:

```java
public static int getS(int m, int n) {
    int S = m / 2;

    if (S % 2 == 0) {
        if (m > 2 * n + 1) {
            S = S - 1;
        }
    }

    return S;
}
```

Khi sinh lịch full, gọi:

```java
int S = getS(canBoFullList.size(), phongThiFullList.size());
```

Tức là `S` được tính từ dữ liệu database:

```text
m = M = tổng số cán bộ trong DB
n = N = tổng số phòng thi trong DB
```

không phải từ `m`, `n` của request hiện tại.

### Điều kiện quan trọng

Phải có:

```text
N <= S
```

Nếu số phòng trong database lớn hơn `S`, thuật toán có thể tạo ra trùng cán bộ hoặc trùng cặp giám thị trong cùng một line.

Nên phải kiểm tra:

```java
if (phongThiFullList.size() > S) {
    throw new Exception("So phong thi full vuot qua S");
}
```

---

## 7. Công thức sinh giám thị

Với phòng có index `i` và line `L`, thuật toán sinh:

```text
Giám thị 1: A(i, L) = (i + L) mod S
Giám thị 2: B(i, L) = S + (i + 2L) mod S
```

Trong code:

```java
int a = (i + line) % S;
int b = S + (i + 2 * line) % S;
```

Sau đó lấy cán bộ:

```java
CanBo giamThi1 = sortedCanBoList.get(a);
CanBo giamThi2 = sortedCanBoList.get(b);
```

---

## 8. Số line tối đa

Vì thuật toán dùng modulo `S`, nếu line chạy quá xa thì kết quả sẽ lặp.

Nếu `S` là số lẻ:

```text
Lmax = S
```

Nếu `S` là số chẵn:

```text
Lmax = S / 2
```

Hàm tính Lmax:

```java
public static int getLmax(int m, int n) {
    int S = getS(m, n);

    if (S % 2 == 0) {
        return S / 2;
    }

    return S;
}
```

Điều kiện bắt buộc:

```text
0 <= line < Lmax
```

Nếu hết line:

```text
line >= Lmax
```

thì hệ thống không còn khả năng tạo thêm ca thi hợp lệ theo thuật toán hiện tại.

---

## 9. Cách tính line và offset

Mỗi line có thể sinh phân công cho toàn bộ `N` phòng.

Với mỗi request mới, hệ thống đọc lịch sử `yeu_cau` để biết line hiện tại đã dùng bao nhiêu phòng.

Nếu line hiện tại còn đủ phòng:

```text
N - soPhongThiDaDung >= n
```

thì dùng tiếp line đó:

```text
line = currentLine
offset = soPhongThiDaDung
```

Nếu không còn đủ phòng:

```text
line = currentLine + 1
offset = 0
```

Code để tính line và offset:

```java
int line = -1;
int offset = 0;

yeuCauFullList.sort((a, b) -> {
    if (a.getLine() != b.getLine()) {
        return Integer.compare(a.getLine(), b.getLine());
    }
    return Long.compare(a.getId(), b.getId());
});

int currentLine = 0;
int soPhongThiDaDung = 0;

for (YeuCau yc : yeuCauFullList) {
    if (yc.getLine() != currentLine) {
        if (sucChuaMotLine - soPhongThiDaDung >= n) {
            line = currentLine;
            offset = soPhongThiDaDung;
            break;
        }

        currentLine = yc.getLine();
        soPhongThiDaDung = 0;
    }

    soPhongThiDaDung += yc.getSoPhongThi();
}

if (line == -1) {
    if (sucChuaMotLine - soPhongThiDaDung >= n) {
        line = currentLine;
        offset = soPhongThiDaDung;
    } else {
        line = currentLine + 1;
        offset = 0;
    }
}

if (line >= Lmax) {
    throw new Exception("Khong con line hop le de tao ca thi moi");
}
```

---

## 10. Pipeline xử lý một request

Luồng xử lý một request:

```text
1. Nhận message n=..., m=...
2. Parse n, m.
3. Lấy toàn bộ cán bộ từ DB.
4. Lấy toàn bộ phòng thi từ DB.
5. Lấy lịch sử yêu cầu từ DB.
6. Tính S và Lmax từ dữ liệu DB.
7. Tính line và offset từ lịch sử yêu cầu.
8. Sinh phân công giám thị full cho line hiện tại.
9. Cắt lấy partition từ offset đến offset + n.
10. Lấy danh sách cán bộ còn lại sau khi đã làm giám thị.
11. Tính số cán bộ giám sát = m - 2n.
12. Chia n phòng thành các khối liên tiếp.
13. Chọn cán bộ giám sát hợp lệ cho từng khối.
14. Kiểm tra tạo đủ số cán bộ giám sát.
15. Xuất Excel gồm 2 sheet.
16. Gửi file kết quả về client.
17. Lưu yêu cầu vào DB.
18. Lưu lịch sử giám sát vào DB.
```

---

## 11. Chứng minh các điều kiện đúng

### 11.1. Một phòng có 2 giám thị khác nhau

Với mọi phòng `i`, line `L`:

```text
a = (i + L) mod S
```

nên:

```text
0 <= a <= S - 1
```

Còn:

```text
b = S + (i + 2L) mod S
```

nên:

```text
S <= b <= 2S - 1
```

Hai khoảng:

```text
[0, S - 1]
[S, 2S - 1]
```

không giao nhau.

Do đó:

```text
a != b
```

Suy ra:

```text
Một phòng luôn có 2 giám thị khác nhau.
```

---

### 11.2. Một cán bộ không coi lại đúng phòng đã coi ở ca khác

Xét một phòng cố định có index `i`.

Giám thị 1 của phòng `i` ở line `L`:

```text
A(i, L) = (i + L) mod S
```

Giả sử giám thị 1 bị lặp ở hai line `L1`, `L2`:

```text
A(i, L1) = A(i, L2)
```

Suy ra:

```text
(i + L1) mod S = (i + L2) mod S
```

Do đó:

```text
L1 ≡ L2 mod S
```

Vì:

```text
0 <= L1, L2 < Lmax <= S
```

nên:

```text
L1 = L2
```

Vậy giám thị 1 không bị lặp lại cho cùng một phòng ở line khác.

Với giám thị 2:

```text
B(i, L) = S + (i + 2L) mod S
```

Nếu bị lặp ở hai line `L1`, `L2`:

```text
B(i, L1) = B(i, L2)
```

thì:

```text
(i + 2L1) mod S = (i + 2L2) mod S
```

Suy ra:

```text
2(L1 - L2) ≡ 0 mod S
```

Nếu `S` lẻ:

```text
gcd(2, S) = 1
```

nên:

```text
L1 ≡ L2 mod S
```

Vì `line < S`, suy ra:

```text
L1 = L2
```

Nếu `S` chẵn, thuật toán chỉ cho phép:

```text
line < S / 2
```

nên `2L mod S` không lặp trong đoạn hợp lệ.

Vậy giám thị 2 cũng không bị lặp lại cho cùng một phòng ở ca khác.

Kết luận:

```text
Một cán bộ không coi lại đúng phòng mình đã coi ở ca thi khác.
```

---

### 11.3. Hai cán bộ đã coi chung không được coi chung lại

Ở phòng `i`, line `L`, cặp giám thị là:

```text
A = (i + L) mod S
B = S + (i + 2L) mod S
```

Giả sử có phòng `j`, line `K` tạo lại đúng cặp đó.

Khi đó:

```text
(i + L) mod S = (j + K) mod S       (1)
(i + 2L) mod S = (j + 2K) mod S     (2)
```

Lấy `(2) - (1)`:

```text
L ≡ K mod S
```

Vì:

```text
0 <= L, K < Lmax <= S
```

nên:

```text
L = K
```

Thay vào `(1)`:

```text
i ≡ j mod S
```

Do điều kiện:

```text
N <= S
```

nên:

```text
0 <= i, j <= N - 1 <= S - 1
```

Vì vậy:

```text
i = j
```

Điều này nghĩa là cặp giám thị chỉ trùng khi cùng line và cùng phòng, tức là cùng một bản ghi phân công, không phải ca khác.

Kết luận:

```text
Hai cán bộ đã coi chung một phòng sẽ không được sinh lại thành cặp coi chung ở phòng khác hoặc ca khác.
```

---

### 11.4. Mỗi request là một ca thi khác

Mỗi request được lưu vào bảng `yeu_cau`.

Từ lịch sử `yeu_cau`, hệ thống tính `line` và `offset`.

Nếu các request trước đã dùng một phần line hiện tại, request mới lấy tiếp từ offset sau đó.

Do vậy các request khác nhau sẽ lấy các đoạn khác nhau trong cùng line, hoặc chuyển sang line mới nếu line cũ không còn đủ phòng.

Điều kiện cần:

```text
Bảng yeu_cau phải được lưu chính xác.
Không được xóa/sửa lịch sử yeu_cau tùy tiện.
```

---

### 11.5. Kết quả có đúng n phòng thi

Request lấy:

```java
phanCongGiamThiFull.subList(offset, offset + n)
```

Đoạn này có đúng `n` phần tử nếu:

```text
offset + n <= N
```

Vì vậy kết quả có đúng `n` phòng thi.

---

### 11.6. Kết quả có đúng m cán bộ

Với `n` phòng, số cán bộ giám thị là:

```text
2n
```

Số cán bộ giám sát cần thêm:

```text
g = m - 2n
```

Nếu `generateGiamSat` tạo đủ `g` cán bộ giám sát khác nhau, thì tổng số cán bộ được dùng:

```text
2n + g = 2n + (m - 2n) = m
```

Do đó kết quả có đúng `m` cán bộ.

Cần kiểm tra sau khi sinh giám sát:

```java
int soGiamSatThucTe = 0;

for (PhanCongGiamSat gs : phanCongGiamSatList) {
    if (gs.getCanBo() != null) {
        soGiamSatThucTe++;
    }
}

if (soGiamSatThucTe != soCanBoGiamSat) {
    throw new Exception("Khong tao du so can bo giam sat theo yeu cau");
}
```

---

### 11.7. Cán bộ giám sát không giám sát lại phòng cũ

Điều kiện này không được bảo đảm bằng công thức giám thị.

Nó được bảo đảm bằng bảng `giam_sat`.

Khi xét một cán bộ cho một khối phòng:

```text
[P_a, P_(a+1), ..., P_b]
```

cần kiểm tra cán bộ đó chưa từng giám sát bất kỳ phòng nào trong khối.

Hàm kiểm tra:

```java
giamSatDAO.coTheGiamSatKhoi(cb.getMaGV(), phongTrongKhoi)
```

Logic:

```text
Nếu tồn tại phòng P trong khối sao cho database đã có cặp:
ma_gv = cán bộ đang xét
phong_thi = P

thì cán bộ đó không hợp lệ cho khối.
```

Nếu chỉ chọn cán bộ khi hàm này trả về `true`, điều kiện giám sát được bảo đảm.

---

### 11.8. Cán bộ từng giám sát vẫn có thể coi thi phòng đó

Khi sinh giám thị, thuật toán không kiểm tra bảng `giam_sat`.

Bảng `giam_sat` chỉ được dùng khi phân công giám sát.

Vì vậy cán bộ từng giám sát phòng `P` vẫn có thể được sinh làm giám thị phòng `P`, miễn là công thức giám thị chưa từng cho cán bộ đó coi phòng `P` trước đó.

Điều này đúng với yêu cầu:

```text
Cán bộ giám sát phòng thi đó thì vẫn có thể đi coi thi lại,
miễn là chưa coi thi phòng đó trước đó.
```

---

## 12. Các giả thiết bắt buộc

Thuật toán đúng nếu các giả thiết sau luôn được giữ:

```text
1. Danh sách cán bộ luôn sort theo id trước khi sinh lịch.
2. Danh sách phòng thi luôn sort theo id trước khi sinh lịch.
3. Không xóa/sửa id cán bộ sau khi đã có lịch sử yeu_cau.
4. Không xóa/sửa id phòng thi sau khi đã có lịch sử yeu_cau.
5. Không xóa/sửa tùy tiện bảng yeu_cau.
6. S được tính cố định từ M và N trong database.
7. N <= S.
8. line < Lmax.
9. offset + n <= N.
10. getCanBoConLai phải dùng maGV như khóa logic, không dùng maGV làm index.
11. generateGiamSat phải kiểm tra bảng giam_sat.
12. Sau khi sinh giám sát phải kiểm tra đủ số cán bộ giám sát thực tế.
13. maGV trong bảng can_bo phải là duy nhất.
14. phongThi trong bảng phong_thi phải là duy nhất.
```

Nếu một trong các giả thiết này bị phá vỡ, thuật toán có thể không còn bảo đảm đúng.

---

## 13. Kết luận

Thuật toán giám thị đúng về mặt toán học nếu dùng mô hình:

```text
Sinh lịch trên toàn bộ database.
Mỗi request lấy một đoạn của line.
Dùng hết line thì chuyển line.
```

Với công thức:

```text
A(i, L) = (i + L) mod S
B(i, L) = S + (i + 2L) mod S
```

thuật toán bảo đảm:

```text
1. Một phòng có 2 giám thị khác nhau.
2. Cán bộ không coi lại phòng đã coi ở ca khác.
3. Hai cán bộ không coi chung lại ở ca khác.
```

Phần giám sát đúng nếu và chỉ nếu:

```text
Có kiểm tra bảng giam_sat trước khi gán cán bộ cho khối phòng.
```

Kết quả cuối cùng đúng yêu cầu `n` phòng và `m` cán bộ nếu:

```text
số giám thị = 2n
số giám sát = m - 2n
generateGiamSat tạo đủ m - 2n cán bộ giám sát thật
```

Tóm lại:

```text
Phần giám thị: chứng minh được bằng công thức.
Phần giám sát: phải kiểm tra bằng database.
Phần tổng số cán bộ m: phải kiểm tra sau khi sinh giám sát.
```

## 14. Hướng dẫn chạy, setup database và cấu hình kết nối mạng

---

### 14.1. Yêu cầu môi trường

Cần chuẩn bị:

```text
Java JDK
Gradle Wrapper có sẵn trong project
MySQL Server
Terminal hoặc Command Prompt / PowerShell
```

Vì project đã có file:

```text
gradlew
gradlew.bat
```

nên không cần cài Gradle toàn cục. Có thể chạy trực tiếp bằng Gradle Wrapper.

---

### 14.2. Cấu trúc project liên quan

Một số file/thư mục quan trọng:

```text
server/src/main/resources/db.sql
server/src/main/java/ckthltm/dal/BaseDAO.java
server/src/main/java/ckthltm/App.java
client/src/main/java/ckthltm/App.java
```

Ý nghĩa:

```text
db.sql      : file SQL để tạo database/table trong MySQL.
BaseDAO.java: nơi cấu hình URL, USER, PASSWORD để kết nối database.
server App : nơi cấu hình port server.
client App : nơi cấu hình địa chỉ server và port để client kết nối.
```

---

### 14.3. Setup database MySQL

File SQL nằm tại:

```text
server/src/main/resources/db.sql
```

#### 14.3.1. Tạo database

Trong MySQL, cần tạo database:

```sql
CREATE DATABASE quan_ly_phong_thi;
```

Sau đó chọn database:

```sql
USE quan_ly_phong_thi;
```

#### 14.3.2. Chạy file `db.sql`

Có thể chạy bằng MySQL CLI:

```bash
mysql -u root -p quan_ly_phong_thi < server/src/main/resources/db.sql
```

Nếu đang ở Windows Command Prompt:

```bat
mysql -u root -p quan_ly_phong_thi < server\src\main\resources\db.sql
```

Nếu dùng MySQL Workbench:

```text
1. Mở MySQL Workbench.
2. Kết nối vào MySQL Server.
3. Chọn database quan_ly_phong_thi.
4. Mở file server/src/main/resources/db.sql.
5. Chạy toàn bộ script.
```

---

### 14.4. Cấu hình database trong server

File cần sửa:

```text
server/src/main/java/ckthltm/dal/BaseDAO.java
```

Trong file này có đoạn:

```java
protected static final String URL = "jdbc:mysql://localhost:3306/quan_ly_phong_thi";
protected static final String USER = "nhatking";
protected static final String PASSWORD = "12345678";
```

Sửa thành thông tin MySQL thật của máy đang chạy server.

Ví dụ:

```java
protected static final String URL = "jdbc:mysql://localhost:3306/quan_ly_phong_thi";
protected static final String USER = "root";
protected static final String PASSWORD = "123456";
```

Giải thích:

```text
localhost              : MySQL đang chạy trên cùng máy với server.
3306                   : port mặc định của MySQL.
quan_ly_phong_thi      : tên database.
USER                   : username MySQL.
PASSWORD               : mật khẩu MySQL.
```

Nếu MySQL chạy ở máy khác trong cùng mạng LAN, có thể đổi `localhost` thành IP của máy chứa MySQL:

```java
protected static final String URL = "jdbc:mysql://192.168.1.10:3306/quan_ly_phong_thi";
```

---

### 14.5. Chạy server bằng Gradle Wrapper

#### 14.5.1. Linux / macOS

Chạy từ thư mục gốc project:

```bash
./gradlew server:run --console=plain
```

#### 14.5.2. Windows

Chạy từ thư mục gốc project:

```bat
gradlew.bat server:run --console=plain
```

---

### 14.6. Cấu hình port server

File cần sửa:

```text
server/src/main/java/ckthltm/App.java
```

Hiện tại có dạng:

```java
public class App {
    public static void main(String[] args) {
        Server server = new Server();
        server.start(8124);
    }
}
```

Nếu muốn đổi port, sửa số `8124`.

Ví dụ đổi sang port `9000`:

```java
public class App {
    public static void main(String[] args) {
        Server server = new Server();
        server.start(9000);
    }
}
```

Lưu ý:

```text
Port server và port client dùng để connect phải giống nhau.
Nếu server dùng 8124 thì client cũng phải connect tới 8124.
Nếu server đổi sang 9000 thì client cũng phải đổi sang 9000.
```

---

### 14.7. Chạy client bằng Gradle Wrapper

#### 14.7.1. Linux / macOS

Chạy từ thư mục gốc project:

```bash
./gradlew client:run --console=plain
```

#### 14.7.2. Windows

Chạy từ thư mục gốc project:

```bat
gradlew.bat client:run --console=plain
```

---

### 14.8. Cấu hình địa chỉ server cho client

File cần sửa:

```text
client/src/main/java/ckthltm/App.java
```

Hiện tại có dạng:

```java
public class App {
    public static void main(String[] args) {
        Client client = new Client();
        try {
            InetAddress address = InetAddress.getLocalHost();
            client.connect(address, 8124);
        } catch (UnknownHostException e) {
            System.err.println(e);
        }
    }
}
```

Đoạn này dùng:

```java
InetAddress.getLocalHost()
```

nghĩa là client sẽ cố kết nối tới chính máy đang chạy client.

Nếu server và client chạy trên cùng một máy thì có thể giữ nguyên.

---

### 14.9. Kết nối qua cùng mạng LAN thay vì localhost

Nếu server chạy trên máy A, client chạy trên máy B, hai máy phải cùng mạng LAN.

Ví dụ:

```text
Máy A chạy server có IP: 192.168.1.20
Máy B chạy client muốn kết nối tới máy A
```

Khi đó sửa `client/src/main/java/ckthltm/App.java` thành:

```java
public class App {
    public static void main(String[] args) {
        Client client = new Client();
        try {
            InetAddress address = InetAddress.getByName("192.168.1.20");
            client.connect(address, 8124);
        } catch (UnknownHostException e) {
            System.err.println(e);
        }
    }
}
```

Nếu server đổi port, ví dụ server dùng port `9000`, thì client cũng phải đổi:

```java
client.connect(address, 9000);
```

---

### 14.10. Cách xem IP của máy chạy server

#### 14.10.1. Windows

Mở Command Prompt:

```bat
ipconfig
```

Tìm dòng:

```text
IPv4 Address
```

Ví dụ:

```text
IPv4 Address . . . . . . . . . . . : 192.168.1.20
```

#### 14.10.2. Linux

Chạy:

```bash
ip addr
```

Hoặc:

```bash
hostname -I
```

#### 14.10.3. macOS

Chạy:

```bash
ifconfig
```

Hoặc vào:

```text
System Settings -> Network
```

---

### 14.11. Lưu ý firewall

Nếu client không kết nối được tới server qua mạng LAN, cần kiểm tra firewall.

Trên máy chạy server, cần cho phép Java hoặc port server đi qua firewall.

Ví dụ nếu server chạy port:

```text
8124
```

thì firewall phải cho phép kết nối tới port `8124`.

Các lỗi thường gặp:

```text
Connection refused     : server chưa chạy hoặc sai port.
Connection timed out   : sai IP, khác mạng, hoặc firewall chặn.
UnknownHostException   : địa chỉ server nhập sai.
```

---

### 14.12. Thứ tự chạy đúng

Nên chạy theo thứ tự:

```text
1. Mở MySQL Server.
2. Tạo database và chạy db.sql nếu chưa setup.
3. Kiểm tra BaseDAO.java đã đúng USER/PASSWORD.
4. Chạy server.
5. Chạy client.
6. Từ client gửi file để nhập liệu database.
7. Từ client gửi yêu cầu n, m để gửi yêu cầu phân công cán bộ
```

Server phải chạy trước client.

---

### 14.13. Lưu ý khi gửi file nhập liệu

Khi gửi file danh sách cán bộ coi thi, tên file bắt buộc phải là:

```text
DanhSachCanBoCoiThi.xlsx
```

Nếu tên file sai, chương trình có thể không đọc đúng file hoặc không nhận diện đúng dữ liệu.

Tên đúng:

```text
DanhSachCanBoCoiThi.xlsx
```

Tên sai ví dụ:

```text
DanhSachCanBo.xlsx
danh_sach_can_bo.xlsx
DanhSachCanBoCoiThi (1).xlsx
```

---

### 14.14. Gợi ý kiểm tra dữ liệu sau khi import

Sau khi import dữ liệu vào database, nên kiểm tra:

```sql
SELECT COUNT(*) FROM can_bo;
SELECT COUNT(*) FROM phong_thi;
SELECT COUNT(*) FROM yeu_cau;
SELECT COUNT(*) FROM giam_sat;
```

Nên kiểm tra `ma_gv` không bị trùng:

```sql
SELECT ma_gv, COUNT(*)
FROM can_bo
GROUP BY ma_gv
HAVING COUNT(*) > 1;
```

Nên kiểm tra phòng thi không bị trùng:

```sql
SELECT phong_thi, COUNT(*)
FROM phong_thi
GROUP BY phong_thi
HAVING COUNT(*) > 1;
```

Nếu có dữ liệu trùng, thuật toán có thể chạy sai vì `maGV` và `phongThi` đang được dùng như khóa logic.

---

### 14.15. Checklist cấu hình nhanh

Trước khi chạy, kiểm tra:

```text
[ ] MySQL đang chạy.
[ ] Database quan_ly_phong_thi đã được tạo.
[ ] Đã chạy server/src/main/resources/db.sql.
[ ] BaseDAO.java đã sửa USER/PASSWORD đúng.
[ ] Server App.java dùng đúng port.
[ ] Client App.java dùng đúng IP server.
[ ] Client App.java dùng đúng port server.
[ ] Nếu chạy qua LAN, firewall cho phép port server.
[ ] File nhập liệu tên đúng: DanhSachCanBoCoiThi.xlsx.
```

---

### 14.16. Lệnh chạy tóm tắt

Linux / macOS:

```bash
./gradlew server:run --console=plain
./gradlew client:run --console=plain
```

Windows:

```bat
gradlew.bat server:run --console=plain
gradlew.bat client:run --console=plain
```

Hoặc PowerShell:

```powershell
.\gradlew.bat server:run --console=plain
.\gradlew.bat client:run --console=plain
```
