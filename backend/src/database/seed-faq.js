const mysql = require('mysql2/promise');
require('dotenv').config();

const pool = mysql.createPool({
    host: process.env.DB_HOST || 'localhost',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '',
    database: process.env.DB_NAME || 'doan3_db',
    port: process.env.DB_PORT || 3306,
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0,
    enableKeepAlive: true,
    keepAliveInitialDelay: 0
});

async function seedFAQs() {
    const conn = await pool.getConnection();
    try {
        console.log('🌱 Seeding FAQ data...');

        // Check if FAQs already exist
        const [existing] = await conn.query('SELECT COUNT(*) as cnt FROM faqs');
        if (existing[0].cnt > 0) {
            console.log(`⚠️  FAQ table already has ${existing[0].cnt} rows. Skipping seed.`);
            console.log('   To re-seed, run: DELETE FROM faqs; first');
            return;
        }

        const faqs = [
            // GENERAL
            ['general', 'Ung dung DoAn3 ho tro nhung loai phuong tien nao?',
                'DoAn3 hien ho tro 3 loai phuong tien:\n1. Xe may - Phu hop cho di chuyen ngan (tu 10.000d co ban + 3.000d/km)\n2. O to 4 cho - Phu hop cho gia dinh nho (tu 12.000d co ban + 5.000d/km)\n3. O to 7 cho - Phu hop cho nhom dong nguoi (tu 15.000d co ban + 7.000d/km)', 1],
            ['general', 'DoAn3 khac gi cac ung dung dat xe khac?',
                '1. Tich hop AI thong minh - goi y lich trinh toi uu\n2. Ghep chuyen (Batch) - tiet kiem chi phi\n3. Theo doi thoi quen di chuyen\n4. Ho tro 24/7\n5. Da dang phuong tien', 2],
            ['general', 'Toi co the su dung DoAn3 o bat ky dia diem nao khong?',
                'Hien tai DoAn3 dang phuc vu tai khu vuc Da Nang. Chung toi se mo rong them nhieu tinh thanh trong thoi gian toi. Ban co the theo doi thong tin cap nhat tren ung dung.', 3],
            ['general', 'DoAn3 co ho tro khach hang khong biet tieng Viet khong?',
                'DoAn3 co the hien thi mot so noi dung bang tieng Anh. Tuy nhien, de duoc ho tro tot nhat, ban vui long lien he nhom cham soc khach hang qua chat trong ung dung.', 4],
            // BOOKING
            ['booking', 'Lam sao de dat xe tren DoAn3?',
                '1. Mo ung dung va dang nhap\n2. Nhan "Dat xe ngay"\n3. Chon diem don va diem den tren ban do\n4. Chon loai phuong tien (Xe may, O to 4 cho, O to 7 cho)\n5. Nhan "Tim tai xe" de xem gia uoc tinh\n6. Nhan "Dat xe ngay" de gui yeu cau', 5],
            ['booking', 'Toi co the dat xe truoc bao lau?',
                'Ban co the dat xe ngay lap tuc hoac dat truoc trong vong 7 ngay. De dat xe truoc, chon ngay va gio tai buoc chon thoi gian khi dat xe.', 6],
            ['booking', 'Toi co the huy chuyen khong? Phi huy la bao nhieu?',
                'Ban co the huy chuyen truoc khi tai xe bat dau.\n- Huy truoc khi tai xe nhan: Khong mat phi\n- Huy sau khi tai xe nhan: 10.000d - 20.000d\n- Huy sau khi tai xe da den: 30.000d - 50.000d', 7],
            ['booking', 'Lam sao de danh gia tai xe sau chuyen di?',
                'Sau khi chuyen di ket thuc, man hinh danh gia se xuat hien tu dong. Ban co the:\n1. Chon so sao tu 1 den 5\n2. Chon cac tag phu hop (an toan, than thien, xe sach, ...)\n3. Viet nhan xet them neu muon', 8],
            ['booking', 'Tai xe co bat buoc cho toi khong?',
                'Co, tai xe bat buoc cho toi tai diem don cua ban. Neu tai xe khong cho, ban co the:\n1. Lien he tai xe qua cuoc goi hoac tin nhan trong ung dung\n2. Bao cao qua muc "Ho tro" trong ung dung\n3. Danh gia tai xe sau chuyen di', 9],
            ['booking', 'Gia cuoc chang di duoc tinh nhu the nao?',
                'Gia cuoc chang = Gia co ban + (Khoang cach x km x Don gia theo km). Gia se duoc hien thi truoc khi ban xac nhan dat xe. Gia co ban tuy thuoc vao loai phuong tien ban chon.', 10],
            // PAYMENT
            ['payment', 'DoAn3 ho tro nhung phuong thuc thanh toan nao?',
                '1. Tien mat (Cash) - Tra truc tiep cho tai xe\n2. VNPay - Thanh toan qua cong thanh toan VNPay\n3. MoMo - Thanh toan qua ung dung MoMo\n4. Vi trong ung dung (Wallet) - Nap tien vao vi de thanh toan nhanh hon', 11],
            ['payment', 'Lam sao nap tien vao vi trong ung dung?',
                '1. Mo ung dung DoAn3\n2. Vao trang ca nhan\n3. Chon "Nap tien"\n4. Chon so tien muon nap\n5. Chon phuong thuc thanh toan\n6. Xac nhan nap tien\nTien se duoc cong vao vi ngay sau khi giao dich thanh cong.', 12],
            ['payment', 'Toi co the xuat hoa don khong?',
                'Co, ban co the yeu cau xuat hoa don sau moi chuyen di. Vao "Lich su chuyen di", chon chuyen di, nhan "Xuat hoa don" va dien thong tin xuat hoa don. Hoa don se duoc gui qua email.', 13],
            ['payment', 'Tai xe co the doi phuong thuc thanh toan khong?',
                'Phuong thuc thanh toan da duoc xac nhan truoc khi dat xe. Tuy nhien, trong mot so truong hop dac biet, tai xe co the hoi ban de doi phuong thuc. Ban co quyen tu choi neu khong dong y.', 14],
            ['payment', 'Phi cong them bao nhieu neu qua gio lam viec?',
                'Hien tai DoAn3 khong ap dung phi cong them theo gio. Gia cuoc chang chi duoc tinh dua tren khoang cach va loai phuong tien. Tuy nhien, gia co the thay doi trong gio cao diem (7h-9h va 17h-19h) do nhu cau tang cao.', 15],
            // ACCOUNT
            ['account', 'Lam sao tao tai khoan tren DoAn3?',
                '1. Tai va cai dat ung dung DoAn3 tu App Store hoac Google Play\n2. Mo ung dung, chon tab "Dang ky"\n3. Nhap thong tin: Ho ten, email, so dien thoai, mat khau\n4. Neu la tai xe, chon "Tai xe" va nhap them thong tin xe\n5. Nhan "Dang ky" de hoan tat', 16],
            ['account', 'Lam sao dat lai mat khau?',
                '1. Nhan "Quen mat khau?" tai man hinh dang nhap\n2. Nhap email da dang ky\n3. Kiem tra email de lay ma OTP 6 so\n4. Nhap ma OTP va dat mat khau moi (toi thieu 6 ky tu)\n5. Nhan "Xac nhan" de hoan tat', 17],
            ['account', 'Toi co the doi email hoac so dien thoai khong?',
                'Ban co the doi so dien thoai trong phan "Chinh sua ho so". Email lien ket voi tai khoan chi co the doi khi lien he nhom ho tro. Vui long bao mat thong tin tai khoan cua ban.', 18],
            ['account', 'Lam sao xoa tai khoan?',
                'Hien tai ban co the yeu cau xoa tai khoan bang cach lien he nhom ho tro qua muc "Ho tro & FAQ" trong ung dung. Tai khoan cua ban se duoc xu ly trong vong 7 ngay lam viec.', 19],
            // DRIVER
            ['driver', 'Toi muon dang ky lam tai xe, can gi?',
                '1. Tai khoan DoAn3 (dang ky nhu khach hang)\n2. Chon loai xe: Xe may, O to 4 cho, hoac O to 7 cho\n3. Nhap thong tin xe: Mau xe, bien so xe\n4. Giay phep lai xe hop le\n5. Hinh chan dung\nSau khi dang ky, tai khoan se duoc xem xet trong 1-2 ngay lam viec.', 20],
            ['driver', 'Thu nhap cua tai xe duoc tinh nhu the nao?',
                'Tai xe nhan 80% gia tri cuoc chang (sau khi tru phi nen tang 20%).\nVD: Cuoc chang 50.000d -> Tai xe nhan 40.000d.\nThu nhap duoc cong vao tai khoan vao cuoi ngay va co the rut ve tai khoan ngan hang bat cu luc nao.', 21],
            ['driver', 'Toi co the choi xe khi khong online khong?',
                'Co, ban hoan toan co the tat che do san sang (Offline) khi khong muon nhan chuyen. Che do online/offline la tuy chon cua ban, khong bat buoc phai online.', 22],
            ['driver', 'Phi nen tang la gi? Phi bao nhieu?',
                'Phi nen tang (Commission) la phi DoAn3 thu de van hanh he thong. Hien tai phi nen tang la 20% tren moi cuoc chang. Phan con lai 80% la thu nhap cua tai xe.', 23],
            // TECHNICAL
            ['technical', 'Ung dung bi lag, lam sao?',
                '1. Khoi dong lai ung dung\n2. Khoi dong lai dien thoai\n3. Kiem tra ket noi internet (Wifi hoac 4G)\n4. Xoa bo nho cache: Cai dat -> Ung dung -> DoAn3 -> Xoa cache\n5. Cap nhat ung dung len phien ban moi nhat tu App Store / Google Play', 24],
            ['technical', 'Tai sao vi tri tai xe tren ban do khong chinh xac?',
                '1. Kiem tra quyen truy cap vi tri cua ung dung trong Cai dat dien thoai\n2. Tat che do tiet kiem pin vi no lam giam do chinh xac GPS\n3. Dam bao dien thoai co ket noi internet on dinh\n4. Khong cho dien thoai trong bao lo vi it kim loai\n5. Thu dong va mo lai GPS trong cai dat dien thoai', 25],
            ['technical', 'Ung dung bi crash, toat ra khi dang su dung?',
                '1. Cap nhat ung dung len phien ban moi nhat\n2. Kiem tra bo nho trong cua dien thoai, giai phong neu can\n3. Lien he ho tro qua muc "Ho tro & FAQ" trong ung dung\n4. Gui ID thiet bi va mo ta loi giup nhom ky thuat xu ly nhanh hon', 26],
            ['technical', 'Toi khong nhan duoc thong bao tu ung dung?',
                '1. Kiem tra quyen thong bao cua DoAn3 trong Cai dat dien thoai\n2. Tat che do "Khong lam phi" hoac "Tiet kiem pin" cho DoAn3\n3. Kiem tra ket noi internet\n4. Thu tat va bat lai thong bao tren dien thoai', 27],
            ['technical', 'Ban do khong tai duoc, lam sao?',
                '1. Kiem tra ket noi internet\n2. Xoa bo nho cache cua ung dung DoAn3\n3. Dam bao ban co Google Maps hoac Google Play Services\n4. Thu cap nhat Google Maps tren dien thoai\n5. Khoi dong lai dien thoai va thu lai', 28],
        ];

        for (const [category, question, answer, display_order] of faqs) {
            await conn.query(
                'INSERT INTO faqs (category, question, answer, display_order) VALUES (?, ?, ?, ?)',
                [category, question, answer, display_order]
            );
        }

        console.log(`✅ Successfully seeded ${faqs.length} FAQ entries!`);
    } finally {
        conn.release();
        await pool.end();
    }
}

seedFAQs().catch(err => {
    console.error('❌ Seed failed:', err.message);
    process.exit(1);
});
