import React, { useEffect, useState } from "react";
import Sidebar from "../sidebar/sidebar";
import BoardHeader from "./boardHeader";
import BoardTable from "./boardTable";
import axios from "axios";

function Community() {
    const [posts, setPosts] = useState([]);
    const category = "소통하기"; // 카테고리 변경시 변경 필요

    useEffect(() => {
        async function fetchPosts() {
            try {
                axios({
                    method: "GET",
                    url: `/${category}/posts`
                }).then((response) => {
                    console.log(response.data);
                    setPosts(response.data);
                });
            } catch (error) {
                console.error("게시물 데이터를 가져오는 중 에러가 발생했습니다.", error);
            }
        }

        fetchPosts();
    }, [category]);

    return (
        <div>
            <Sidebar />
            <BoardHeader boardTitle={category} />
            <BoardTable posts={posts} />
        </div>
    );
}

export default Community;
